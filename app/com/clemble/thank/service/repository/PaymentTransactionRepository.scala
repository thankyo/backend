package com.clemble.thank.service.repository

import com.clemble.thank.model.{PaymentTransaction}

import scala.concurrent.Future

trait PaymentTransactionRepository extends UserAwareRepository[PaymentTransaction] {

  def save(tr: PaymentTransaction): Future[PaymentTransaction]

}
