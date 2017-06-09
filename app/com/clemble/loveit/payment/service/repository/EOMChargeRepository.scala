package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import com.clemble.loveit.payment.model.EOMCharge
import com.clemble.loveit.user.service.repository.UserAwareRepository
import org.joda.time.DateTime
import play.api.libs.json.JsValue

import scala.concurrent.Future

/**
  * EOMChargeRepository, responsible for storing and update EOMCharge
  */
trait EOMChargeRepository extends UserAwareRepository[EOMCharge] {

  /**
    * Lists all charges with specified status
    *
    * @param yom year of month
    * @return list of charges with specified status
    */
  def listPending(yom: YearMonth): Source[EOMCharge, _]

  /**
    * Updates charge status, specifying details and status for pending Charge,
    *
    *
    * @param user target user
    * @param yom year of month
    * @param status new status
    * @param details transaction details
    * @return true if processing was Successful, false otherwise
    *
    * @throws RepositoryException update only of pending charges is allowed, others will cause error,
    *                             that must be addressed by the admin
    */
  @throws[RepositoryException]
  def updatePending(user: UserID, yom: YearMonth, status: ChargeStatus, details: JsValue): Future[Boolean]

  /**
    * Saves charge for future processing
    *
    * @param charge charge to process
    * @return saved EOMCharge
    */
  def save(charge: EOMCharge): Future[EOMCharge]

}
