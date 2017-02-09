package com.clemble.thank.service

import com.clemble.thank.model.{Amount, Payment, User}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

trait UserPaymentService {

  def payments(user: User): Enumerator[Payment]

  def debit(user: User, amount: Amount): Future[Payment]

  def credit(user: User, amount: Amount): Future[Payment]

}
