package com.clemble.loveit.payment.service.repository

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.user.service.repository.UserRepository

import scala.concurrent.Future

trait BalanceService {

  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}

@Singleton
case class SimpleBalanceService @Inject() (userRepo: UserRepository) extends BalanceService {

  override def updateBalance(user: UserID, change: Amount): Future[Boolean] = userRepo.changeBalance(user, change)

}
