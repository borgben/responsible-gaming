package domain
import java.time.{LocalDateTime}

final case class ThresholdExceeded(CustomerGuid:String, ThresholdExceededAmount:Double, DateTime:LocalDateTime, KafkaDateTime: LocalDateTime)
final case class CustomerLoss(CustomerGuid:String, TotalMoneyAmount:Double, DateTime:LocalDateTime)