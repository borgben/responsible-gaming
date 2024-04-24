package implicits
import domain.CustomerLoss
import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.kstream.Consumed
import org.apache.kafka.streams.scala.kstream.Produced
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Implicits {
    implicit def consumedFromSerde[K, V](implicit keySerde: Serde[K], valueSerde: Serde[V]): Consumed[K, V] = Consumed.`with`[K, V];
    implicit def producedFromSerde[K, V](implicit keySerde: Serde[K], valueSerde: Serde[V]): Produced[K, V] = Produced.`with`[K, V];
    
    implicit def serde[A >: Null : Decoder : Encoder]: Serde[A] = {

        // Takes an object of type A encodes it as a JSON string and then serialises to an array of Bytes.
        val serializer = (a: A) => a.asJson.noSpaces.getBytes

        // Takes an array of bytes decodes it into a String and then attempts to build an object of type A.
        val deserializer = (aAsBytes: Array[Byte]) => {
            val aAsString = new String(aAsBytes)
            val aOrError = decode[A](aAsString)
            aOrError match {
                case Right(a)    => Option(a)
                case Left(error) =>{
                    println(s"There was an error converting the message $aOrError, $error")
                    Option.empty
                }
            }
        }

        // Returns a Serde[A].
        Serdes.fromFn[A](serializer, deserializer)
  }
}
