package com.clemble.thank.payment.service.repository

import com.clemble.thank.payment.model.PaymentTransaction
import com.clemble.thank.service.repository.UserAwareRepository

import scala.concurrent.Future

trait PaymentTransactionRepository extends UserAwareRepository[PaymentTransaction] {

  def save(tr: PaymentTransaction): Future[PaymentTransaction]

}
