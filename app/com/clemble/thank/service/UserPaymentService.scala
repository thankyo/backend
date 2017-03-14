package com.clemble.thank.service

import akka.stream.scaladsl.Source
import com.clemble.thank.model._

import scala.concurrent.Future

trait UserPaymentService {

  def payments(user: UserId): Source[Payment, _]

  def operation(giver: UserId, url: URI, amount: Amount): Future[List[Payment]]

  def debit(user: User, amount: Amount): Future[Payment]

  def credit(user: User, amount: Amount): Future[Payment]

}
