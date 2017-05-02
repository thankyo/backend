package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.user.service.UserService

import scala.concurrent.Future

trait ThankBalanceService {

  /**
    * Updates user balance
    *
    * @param user user identifier
    * @param change amount of change
    * @return true if enough funds were available
    */
  @throws[UserException]
  def update(user: UserID, change: Amount): Future[Boolean]

}

@Singleton
case class UserThankBalanceService @Inject()(userService: UserService) extends ThankBalanceService {

  override def update(user: UserID, change: Amount): Future[Boolean] = {
    userService.updateBalance(user, change)
  }

}


