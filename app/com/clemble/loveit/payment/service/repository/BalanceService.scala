package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.model.{Amount, UserID}

import scala.concurrent.Future

trait BalanceService {

  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}