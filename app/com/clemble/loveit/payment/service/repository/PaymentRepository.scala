package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.payment.model.BankDetails

import scala.concurrent.Future

trait PaymentRepository {

  def updateBalance(user: UserID, change: Amount): Future[Boolean]

  def getBankDetails(user: UserID): Future[Option[BankDetails]]

  def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean]

}