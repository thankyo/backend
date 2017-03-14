package com.clemble.thank.service.impl

import akka.stream.scaladsl.Source
import com.clemble.thank.model.{Amount, Payment, User, UserId}
import com.clemble.thank.service.repository.PaymentRepository
import com.clemble.thank.service.{UserPaymentService, UserService}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserPaymentService @Inject()(userService: UserService, repository: PaymentRepository, implicit val ec: ExecutionContext) extends UserPaymentService {

  override def payments(user: UserId): Source[Payment, _] = {
    repository.findByUser(user)
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
