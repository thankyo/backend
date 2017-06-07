package com.clemble.loveit.payment.service.repository

import akka.stream.scaladsl.Source
import com.clemble.loveit.payment.model.UserPayment

trait UserPaymentRepository {

  def find(): Source[UserPayment, _]

}
