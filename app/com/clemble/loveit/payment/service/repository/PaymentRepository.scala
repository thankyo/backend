package com.clemble.loveit.payment.service.repository

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.{ChargeAccount, Money, PayoutAccount, UserPayment}

import scala.concurrent.Future

trait PaymentLimitRepository {

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

trait PaymentAccountRepository {

  /**
    * Get user bank details
    *
    * @param user user identifier
    * @return optional user [[ChargeAccount]]
    */
  def getChargeAccount(user: UserID): Future[Option[ChargeAccount]]


  /**
    * Set user bank details
    *
    * @param user user identifier
    * @param chargeAccount new [[ChargeAccount]]
    * @return true, if updated was successful, false otherwise
    */
  def setChargeAccount(user: UserID, chargeAccount: ChargeAccount): Future[Boolean]

  /**
    * Retrieve [[PayoutAccount]]
    */
  def getPayoutAccount(user: UserID): Future[Option[PayoutAccount]]

  /**
    * Set [[PayoutAccount]] for the user
    */
  def setPayoutAccount(user: UserID, payoutAccount: PayoutAccount): Future[Boolean]
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

trait UserPaymentRepository {

  def save(userPayment: UserPayment): Future[Boolean]

  def findById(id: UserID): Future[Option[UserPayment]]

  def find(): Source[UserPayment, _]

}

trait PaymentRepository extends PaymentLimitRepository with PaymentAccountRepository with BalanceRepository with UserPaymentRepository {
}