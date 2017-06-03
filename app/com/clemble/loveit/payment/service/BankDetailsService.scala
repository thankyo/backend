package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.stream.scaladsl.Balance
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.payment.service.repository.PaymentRepository
import com.clemble.loveit.user.service.UserService

import scala.concurrent.{ExecutionContext, Future}

/**
  * Service responsible for storing bank details of the users
  */
trait BankDetailsService {

  /**
    * Get user bank details
    *
    * @param userId user identifier
    * @return optional user BankDetails
    */
  def get(userId: UserID): Future[Option[BankDetails]]

  /**
    * Set user bank details
    *
    * @param userId user identifier
    * @param details new BankDetails
    * @return true, if updated was successful, false otherwise
    */
  def set(userId: UserID, details: BankDetails): Future[Boolean]

}

@Singleton
case class UserBankDetailsService @Inject() (paymentRepo: PaymentRepository, implicit val ec: ExecutionContext) extends BankDetailsService {

  override def get(userId: UserID): Future[Option[BankDetails]] = {
    paymentRepo.getBankDetails(userId)
  }

  override def set(userId: UserID, details: BankDetails): Future[Boolean] = {
    paymentRepo.setBankDetails(userId, details)
  }

}


