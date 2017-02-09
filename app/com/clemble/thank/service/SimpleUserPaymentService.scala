package com.clemble.thank.service

import com.clemble.thank.model.{Amount, Payment, User}
import com.clemble.thank.service.repository.{PaymentRepository, UserRepository}
import com.google.inject.{Inject, Singleton}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

@Singleton
case class SimpleUserPaymentService @Inject() (repository: PaymentRepository) extends UserPaymentService{

  override def payments(user: User): Enumerator[Payment] = {
    repository.findByUser(user.id)
  }

  override def debit(user: User, amount: Amount): Future[Payment] = {
    val debitOperation = Payment.debit(user, amount)
    repository.save(debitOperation)
  }

  override def credit(user: User, amount: Amount): Future[Payment] = {
    val creditOperation = Payment.credit(user, amount)
    repository.save(creditOperation)
  }

}
