package KStream

import org.apache.kafka.streams.scala.StreamsBuilder
import java.util.Properties;
import java.time.Duration;
import implicits.Implicits._
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.scala.kstream.KStream
import org.apache.kafka.streams.scala.kstream.Consumed
import org.apache.kafka.streams.scala.kstream.Produced
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.Serdes.Double
import io.circe.generic.auto._
import cats.effect.kernel.Resource
import cats.effect.IO
import skunk.Session
import org.apache.kafka.streams.{KafkaStreams}
import java.time.LocalDateTime
import java.util.Date
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.format.DateTimeFormatter
import com.typesafe.config._
import java.io.FileNotFoundException
import scala.io.Source

import domain._
import dbConnection.ConnectionPool
import implicits.Implicits.serde
import controller.CustomerLossController
import dbService._

object Main extends App {

  val properties: Properties = new Properties()
  val source = Source.fromFile("src/main/resources/database.properties")
  properties.load(source.bufferedReader())

  val settings = new Properties
  val boostrapServerConfig = s"${properties.getProperty("kafka_hostname")}:${properties.getProperty("kafka_port")}"
  settings.put(StreamsConfig.APPLICATION_ID_CONFIG, properties.getProperty("kafka_consumer_group"))
  settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, boostrapServerConfig)
  settings.put("default.deserialization.exception.handler",classOf[LogAndContinueExceptionHandler])

  val connectionPool: Resource[IO, Resource[IO, Session[IO]]] = ConnectionPool.pool(
        properties.getProperty("postgres_hostname"), 
        properties.getProperty("postgres_port").toInt, 
        properties.getProperty("postgres_username"), 
        properties.getProperty("postgres_database"), 
        properties.getProperty("postgres_password"), 
        properties.getProperty("postgres_max_pool_size").toInt
  );

  val customerLossServicePool: Resource[IO,Resource[IO, Service[IO]]] =
    connectionPool.map(_.map(Service.fromSession(_)));

  implicit val consumer: Consumed[String,CustomerLoss] = consumedFromSerde(Serdes.String(),serde[CustomerLoss]);
  implicit val producer: Produced[CustomerLoss,String] = producedFromSerde(serde[CustomerLoss],Serdes.String());

  val builder = new StreamsBuilder();

  val lossStream: KStream[String,CustomerLoss] = builder.stream("loss-events")

  lossStream.mapValues(
    customerLoss => 
      CustomerLossController.customerLossHandler(customerLoss,customerLossServicePool)
  ).filter((k,v) => v != None).to("big-losses");

  val streams: KafkaStreams = new KafkaStreams(builder.build(), settings);

  streams.cleanUp()
  streams.start()

  // Add shutdown hook to respond to SIGTERM and gracefully close Kafka Streams
  sys.ShutdownHookThread {
    println("Shutting Down");
    streams.close(Duration.ofSeconds(10))
  }
}