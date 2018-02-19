package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.payment.model.PendingTransaction

import scala.concurrent.Future

/**
  * [[PendingTransaction]] repository
  */
trait PendingTransactionRepository {

  def save(user: UserID, payment: PendingTransaction): Future[Boolean]

  def findUsersWithPayouts(): Future[List[UserID]]

  def findUsersWithoutCharges(): Future[List[UserID]]

  def findOutgoingByUser(user: UserID): Future[List[PendingTransaction]]

  def findIncomingByUser(user: UserID): Future[List[PendingTransaction]]

  def removeCharges(user: UserID, thanks: Seq[PendingTransaction]): Future[Boolean]

  def removePayouts(user: UserID, transaction: Seq[PendingTransaction]): Future[Boolean]

}
