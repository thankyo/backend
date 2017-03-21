package com.clemble.thank.service

import com.clemble.thank.model._
import com.clemble.thank.model.error.UserException
import com.clemble.thank.payment.model.BankDetails

import scala.concurrent.Future

trait UserService {

  def findById(userId: UserID): Future[Option[User]]

  def assignOwnership(userId: UserID, ownership: ResourceOwnership): Future[ResourceOwnership]

  def findResourceOwner(uri: Resource): Future[User]

  def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean]

  /**
    * Updates user balance
    *
    * @param user user identifier
    * @param change amount of change
    * @return true if enough funds were available
    */
  @throws[UserException]
  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}
