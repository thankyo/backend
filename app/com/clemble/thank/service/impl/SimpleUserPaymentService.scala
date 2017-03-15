package com.clemble.thank.service.impl

import akka.stream.scaladsl.Source
import com.clemble.thank.model._
import com.clemble.thank.service.repository.PaymentRepository
import com.clemble.thank.service.{UserPaymentService, UserService}
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserPaymentService @Inject()(userService: UserService, repository: PaymentRepository, implicit val ec: ExecutionContext) extends UserPaymentService {

  override def payments(user: UserID): Source[Payment, _] = {
    repository.findByUser(user)
  }

  override def operation(giverId: UserID, url: Resource, amount: Amount): Future[List[Payment]] = {
    for {
      owner <- userService.findResourceOwner(url)
      ownerDebitOp <- debit(owner.id, url, amount)
      giverCreditOp <- credit(giverId,url,  amount)
    } yield {
      List(ownerDebitOp, giverCreditOp)
    }
  }

  private def debit(user: UserID, uri: Resource, amount: Amount): Future[Payment] = {
    val debitOperation = Payment.debit(user, uri, amount)
    for {
      _ <- userService.updateBalance(user, amount)
      payment <- repository.save(debitOperation)
    } yield {
      payment
    }
  }

  private def credit(user: UserID, uri: Resource, amount: Amount): Future[Payment] = {
    val creditOperation = Payment.credit(user, uri, amount)
    for {
      _ <- userService.updateBalance(user, -amount)
      payment <- repository.save(creditOperation)
    } yield {
      payment
    }
  }

}
