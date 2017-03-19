package com.clemble.thank.service

import com.clemble.thank.model._
import com.clemble.thank.payment.model.BankDetails

import scala.concurrent.Future

trait UserService {

  def findById(userId: UserID): Future[Option[User]]

  def assignOwnership(userId: UserID, ownership: ResourceOwnership): Future[ResourceOwnership]

  def findResourceOwner(uri: Resource): Future[User]

  def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean]

  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}
