package com.clemble.loveit.payment.service

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{PaymentRequest, PaymentTransaction}

import scala.concurrent.Future

/**
  * Payment processing service abstraction
  */
trait PaymentService {

  /**
    *
    * @param user
    * @param req
    * @return
    */
  def process(user: UserID, req: PaymentRequest): Future[PaymentTransaction]

}
