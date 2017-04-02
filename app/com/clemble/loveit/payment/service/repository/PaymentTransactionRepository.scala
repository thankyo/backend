package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.payment.model.PaymentTransaction
import com.clemble.loveit.user.service.repository.UserAwareRepository

import scala.concurrent.Future

trait PaymentTransactionRepository extends UserAwareRepository[PaymentTransaction] {

  def save(tr: PaymentTransaction): Future[PaymentTransaction]

}
