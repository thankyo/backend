package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.payment.model.PendingTransaction

import scala.concurrent.Future

/**
  * [[PendingTransaction]] repository
  */
trait PendingTransactionRepository {

  def save(user: UserID, payment: PendingTransaction): Future[Boolean]

  def findOutgoingByUser(user: UserID): Future[List[PendingTransaction]]

  def findIncomingByUser(user: UserID): Future[List[PendingTransaction]]

  def removeOutgoing(user: UserID, thanks: Seq[PendingTransaction]): Future[Boolean]

  def removeIncoming(user: UserID, transaction: Seq[PendingTransaction]): Future[Boolean]

}
