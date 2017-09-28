package com.clemble.loveit.payment.service.repository

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.UserPayment

import scala.concurrent.Future

trait UserPaymentRepository {

  def findById(id: UserID): Future[Option[UserPayment]]

  def find(): Source[UserPayment, _]

}
