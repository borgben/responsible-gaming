package controller

import cats.effect.IO
import skunk.Session
import skunk._
import skunk.implicits._
import skunk.codec.all._
import cats.effect.kernel.Resource
import java.time.{LocalDateTime, Instant}
import cats.effect.unsafe.implicits.global

import domain._
import dbService._

object CustomerLossController{ 
  def customerLossHandler(customerLossEvent:CustomerLoss, customerLossServicePool:Resource[IO, Resource[IO, Service[IO]]]): Option[ThresholdExceeded] = {
      val computeThreshold: Double => Option[ThresholdExceeded] = x => if (x > 1000) Some(ThresholdExceeded(customerLossEvent.CustomerGuid, x, customerLossEvent.DateTime, LocalDateTime.now)) else None

      val customerAgg: Double = customerLossServicePool.use{
        customerLossService => 
          customerLossService.use{
            s => 
              for {
                agg <- s.getAggregatedLossByCustomer(customerLossEvent)
                _   <- s.insertCustomerLoss(customerLossEvent)
              } yield agg
          }
      }.unsafeRunSync

      computeThreshold(customerAgg)
  }
}