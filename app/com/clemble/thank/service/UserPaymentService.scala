package com.clemble.thank.service

import akka.stream.scaladsl.Source
import com.clemble.thank.model._

import scala.concurrent.Future

trait UserPaymentService {

  def payments(user: UserID): Source[Payment, _]

  def operation(giver: UserID, url: Resource, amount: Amount): Future[List[Payment]]

}
