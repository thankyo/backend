package com.clemble.thank.service

import com.clemble.thank.model.{Amount, Payment, User}

import scala.concurrent.Future

trait PaymentService {

  def pay(user: User, amount: Amount): Future[Payment]

  def receive(user: User, amount: Amount): Future[Payment]

  def systemBalance(): Future[Amount]

}
