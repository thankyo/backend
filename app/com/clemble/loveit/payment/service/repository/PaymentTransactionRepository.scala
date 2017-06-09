package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.payment.model.Charge
import com.clemble.loveit.user.service.repository.UserAwareRepository

import scala.concurrent.Future

trait PaymentTransactionRepository extends UserAwareRepository[Charge] {

  def save(tr: Charge): Future[Charge]

}
