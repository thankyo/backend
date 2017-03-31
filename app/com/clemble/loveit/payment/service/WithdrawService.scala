package com.clemble.loveit.payment.service

import com.clemble.loveit.payment.model.{BankDetails, Money}

import scala.concurrent.{Future}

trait WithdrawService {

  def withdraw(money: Money, account: BankDetails): Future[Boolean]

}

class EmptyWithdrawService extends WithdrawService {
  override def withdraw(money: Money, account: BankDetails): Future[Boolean] = Future.successful(true)
}