package com.clemble.loveit.payment.model

import java.time.YearMonth

import com.clemble.loveit.common.model.CreatedAware
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
case class EOMPaymentStatus(
                             yom: YearMonth,
                             createCharges: EOMStatistics = EOMStatistics(),
                             applyCharges: EOMStatistics = EOMStatistics(),
                             createPayout: EOMStatistics = EOMStatistics(),
                             applyPayout: EOMStatistics = EOMStatistics(),
                             created: DateTime
              ) extends CreatedAware with EOMAware{

  def isValid = createCharges.isValid &&
    applyCharges.isValid &&
    createPayout.isValid &&
    applyPayout.isValid

}

object EOMPaymentStatus {

  private implicit val statJsonFormat = Json.format[EOMStatistics]
  implicit val jsonFormat = Json.format[EOMPaymentStatus]

}
