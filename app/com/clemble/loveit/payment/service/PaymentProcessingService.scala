package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.payment.model._

import scala.concurrent.Future

/**
  * Payment processing service abstraction
  */
trait PaymentProcessingService[T <: PaymentRequest] {

  /**
    * Process payment request by the user
    *
    * @param req update request
    * @return created transaction
    */
  def process(req: T): Future[(String, BankDetails, Money)]

}

@Singleton
case class PaymentProcessingServiceFacade @Inject() (
                                  payPalService: PaymentProcessingService[BraintreePaymentRequest],
                                  stripeService: PaymentProcessingService[StripePaymentRequest]
                                ) extends PaymentProcessingService[PaymentRequest] {

  def process(req: PaymentRequest): Future[(String, BankDetails, Money)] = {
    req match {
      case ppbd : BraintreePaymentRequest => payPalService.process(ppbd)
      case stripe: StripePaymentRequest => stripeService.process(stripe)
    }
  }

}


