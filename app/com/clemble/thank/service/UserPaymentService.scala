package com.clemble.thank.service

import akka.stream.scaladsl.Source
import com.clemble.thank.model.{Amount, Payment, User, UserId}

import scala.concurrent.Future

trait UserPaymentService {

  def payments(user: UserId): Source[Payment, _]

  def debit(user: User, amount: Amount): Future[Payment]

  def credit(user: User, amount: Amount): Future[Payment]

}
