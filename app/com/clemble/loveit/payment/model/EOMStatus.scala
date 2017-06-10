package com.clemble.loveit.payment.model

import java.time.YearMonth

import com.clemble.loveit.common.model.CreatedAware
import com.clemble.loveit.common.util.WriteableUtils
import org.joda.time.DateTime
import play.api.libs.json.Json

/**
  * EOM statistics
  *
  * @param total number of all subjects for processing
  * @param pending number of pending subjects for processing
  * @param success number of successfully processed
  * @param failed number of failed processings
  */
case class EOMStatistics(
                    total: Long = 0,
                    pending: Long = 0,
                    success: Long = 0,
                    failed: Long = 0
                  ) {

  def isValid = total == (pending + success + failed)

  def incSuccess() = copy(total = total + 1, success = success + 1)
  def incFailure() = copy(total = total + 1, failed = failed + 1)
}

object EOMStatistics {
  implicit val statJsonFormat = Json.format[EOMStatistics]
}

/**
  * EOM processing status generator
  *
  * @param createCharges statistics for applying charges
  * @param applyCharges statistics for applying charges, total is number of success from previous step
  * @param createPayout statistics for generated payouts
  * @param applyPayout statistics for applying payouts
  * @param created creation time
  */
case class EOMStatus(
                             yom: YearMonth,
                             createCharges: EOMStatistics = EOMStatistics(),
                             applyCharges: EOMStatistics = EOMStatistics(),
                             createPayout: EOMStatistics = EOMStatistics(),
                             applyPayout: EOMStatistics = EOMStatistics(),
                             finished: Option[DateTime] = None,
                             created: DateTime = DateTime.now()
              ) extends CreatedAware with EOMAware{

  def isValid = createCharges.isValid &&
    applyCharges.isValid &&
    createPayout.isValid &&
    applyPayout.isValid

}

object EOMStatus {

  implicit val jsonFormat = Json.format[EOMStatus]

  implicit val writeableFormat = WriteableUtils.jsonToWriteable[EOMStatus]()

}
