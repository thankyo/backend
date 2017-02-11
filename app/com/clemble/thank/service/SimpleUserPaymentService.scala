package com.clemble.thank.service

import com.clemble.thank.model.{Amount, Payment, User}
import com.clemble.thank.service.repository.{PaymentRepository, UserRepository}
import com.google.inject.{Inject, Singleton}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserPaymentService @Inject() (userService: UserService, repository: PaymentRepository, implicit val ec: ExecutionContext) extends UserPaymentService{

  override def payments(user: User): Enumerator[Payment] = {
    repository.findByUser(user.id)
  }

  override def debit(user: User, amount: Amount): Future[Payment] = {
    val debitOperation = Payment.debit(user, amount)
    for {
      _ <- userService.updateBalance(user.id, amount)
      payment <- repository.save(debitOperation)
    } yield {
      payment
    }
  }

  override def credit(user: User, amount: Amount): Future[Payment] = {
    val creditOperation = Payment.credit(user, amount)
    for {
      _ <- userService.updateBalance(user.id, -amount)
      payment <- repository.save(creditOperation)
    } yield {
      payment
    }
  }

}
