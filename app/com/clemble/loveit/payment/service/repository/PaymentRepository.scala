package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.BankDetails

import scala.concurrent.Future

trait PaymentRepository {

  /**
    * @return 0 if user is missing, or has no activity, otherwise returns current user balance
    */
  def getBalance(user: UserID): Future[Amount]

  /**
    * Changes user balance
    *
    * @return true if operation proceeded as expected, false otherwise
    */
  def updateBalance(user: UserID, change: Amount): Future[Boolean]

  /**
    * Get user bank details
    *
    * @param user user identifier
    * @return optional user BankDetails
    */
  def getBankDetails(user: UserID): Future[Option[BankDetails]]

  /**
    * Set user bank details
    *
    * @param user user identifier
    * @param bankDetails new BankDetails
    * @return true, if updated was successful, false otherwise
    */
  def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean]

}