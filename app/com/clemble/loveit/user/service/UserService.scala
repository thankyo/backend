package com.clemble.loveit.user.service

import com.clemble.loveit.user.model._
import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.thank.service.ResourceOwnershipService

import scala.concurrent.Future

trait UserService {

  def findById(userId: UserID): Future[Option[User]]

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
