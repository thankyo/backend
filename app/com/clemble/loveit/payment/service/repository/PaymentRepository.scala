package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.{BankDetails, Money}

import scala.concurrent.Future

trait MonthlyLimitRepository {

  /**
    * Get monthly limit
    */
  def getMonthlyLimit(user: UserID): Future[Option[Money]]

  /**
    * Sets transaction limit for specified User
    *
    * @return true if update was success, false otherwise
    */
  def setMonthlyLimit(user: UserID, monthlyLimit: Money): Future[Boolean]

}

trait BankDetailsRepository {

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

trait BalanceRepository {

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

}

trait PaymentRepository extends MonthlyLimitRepository with BankDetailsRepository with BalanceRepository {
}