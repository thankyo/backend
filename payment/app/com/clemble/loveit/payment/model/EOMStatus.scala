package com.clemble.loveit.payment.model

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.common.model.CreatedAware
import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json.{Json, OFormat}

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

  def isValid: Boolean = total == (pending + success + failed)

  def incSuccess(): EOMStatistics = copy(total = total + 1, success = success + 1)
  def incFailure(): EOMStatistics = copy(total = total + 1, failed = failed + 1)
}

object EOMStatistics {
  implicit val statJsonFormat: OFormat[EOMStatistics] = Json.format[EOMStatistics]
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
                             createCharges: Option[EOMStatistics] = None,
                             applyCharges: Option[EOMStatistics] = None,
                             createPayout: Option[EOMStatistics] = None,
                             applyPayout: Option[EOMStatistics] = None,
                             finished: Option[LocalDateTime] = None,
                             created: LocalDateTime = LocalDateTime.now()
              ) extends CreatedAware with EOMAware{

  def isValid: Boolean = createCharges.exists(_.isValid) &&
    applyCharges.exists(_.isValid) &&
    createPayout.exists(_.isValid) &&
    applyPayout.exists(_.isValid)

}

object EOMStatus {

  implicit val jsonFormat: OFormat[EOMStatus] = Json.format[EOMStatus]

  implicit val writeableFormat: Writeable[EOMStatus] = WriteableUtils.jsonToWriteable[EOMStatus]()

}
