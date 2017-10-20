package com.clemble.loveit.payment.service.repository

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.{ThankTransaction, UserID}
import com.clemble.loveit.payment.model.PendingTransaction

import scala.concurrent.Future

/**
  * [[ThankTransaction]] repository
  */
trait PendingTransactionRepository {

  def save(user: UserID, payment: PendingTransaction): Future[Boolean]

  def findByUser(user: UserID): Source[PendingTransaction, _]

  def removeAll(user: UserID, thanks: Seq[PendingTransaction]): Future[Boolean]

}
