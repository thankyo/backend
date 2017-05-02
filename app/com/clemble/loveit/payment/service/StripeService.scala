package com.clemble.loveit.payment.service

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{Money, StripeBankDetails}

import scala.concurrent.Future

/**
  * Stipe processing service
  */
trait StripeService {

  def create(user: UserID, customer: String, amount: Money): Future[StripeBankDetails]

}
