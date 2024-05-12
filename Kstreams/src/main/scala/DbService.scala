package dbService

import cats.syntax.all._
import cats.effect._
import skunk._
import skunk.implicits._
import skunk.codec.all._
import java.time.OffsetDateTime
import natchez.Trace.Implicits.noop
import fs2.Stream
import cats.Monad
import java.time.LocalDateTime

import domain._

trait Service[F[_]] {
  def getAggregatedLossByCustomer(customerLossEvent:CustomerLoss): F[Double]
  def insertCustomerLoss(customerLossEvent:CustomerLoss): F[Unit]
}

object Service {

  private val aggregatedLossByCustomer: Query[String ~ LocalDateTime ~ LocalDateTime, Double] = 
    sql"""
          SELECT
              COALESCE(SUM(REAL_MONEY),0)
          FROM
              DATA_RADAR.BIG_LOSSES as BL
          WHERE
              BL.CUSTOMER_GUID = $varchar AND ((BL.EVENT_TIMESTAMP <=  $timestamp) AND (BL.EVENT_TIMESTAMP >= ( $timestamp - INTERVAL '1 day')));
    """
    .query(float8)

  private val insertCustomerLossRecord: Command[String ~ Double ~ LocalDateTime] = 
    sql"""
          INSERT INTO DATA_RADAR.BIG_LOSSES(customer_guid, real_money, event_timestamp)
              VALUES ($varchar, $float8, $timestamp);
    """
    .command

  def fromSession[F[_]: Monad](s: Session[F]): Service[F] =     
    new Service[F] {
        def getAggregatedLossByCustomer(customerLossEvent: CustomerLoss): F[Double] = s.prepare(aggregatedLossByCustomer).flatMap{_.unique(customerLossEvent.CustomerGuid ~ customerLossEvent.DateTime ~ customerLossEvent.DateTime)}
        def insertCustomerLoss(customerLossEvent: CustomerLoss): F[Unit] = s.prepare(insertCustomerLossRecord).flatMap{_.execute(customerLossEvent.CustomerGuid ~ customerLossEvent.TotalMoneyAmount ~ customerLossEvent.DateTime).void}
    }
}