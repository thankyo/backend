package com.clemble.loveit.payment.service

import javax.inject.{Singleton}

import com.clemble.loveit.payment.model._
import com.google.common.collect.Maps

import scala.concurrent.Future

/**
  * Payment processing service abstraction
  */
sealed trait PaymentProcessingService[T <: PaymentRequest] {

  /**
    * Process payment request by the user
    *
    * @param req update request
    * @return created transaction
    */
  def process(req: T): Future[(String, BankDetails, Money)]

}

/**
  * Stipe processing service
  */
trait StripeProcessingService extends PaymentProcessingService[PaymentRequest]

import com.stripe.model.Charge
import com.stripe.model.Customer

@Singleton
case object StripeProcessingService extends StripeProcessingService {

  def charge(bankDetails: StripeBankDetails, amount: Money): Charge = {
    val chargeParams = Maps.newHashMap[String, Object]()
    val stripeAmount = (amount.amount * 100).toInt
    chargeParams.put("amount", stripeAmount.toString)
    chargeParams.put("currency", amount.currency.getCurrencyCode.toLowerCase())
    chargeParams.put("customer", bankDetails.customer)
    Charge.create(chargeParams)
  }

  def createCustomer(token: String): Customer = {
    val customerParams = Maps.newHashMap[String, Object]()
    customerParams.put("source", token)
    Customer.create(customerParams)
  }

  override def process(req: PaymentRequest): Future[(String, BankDetails, Money)] = {
    val customer = createCustomer(req.asInstanceOf[StripePaymentRequest].token)
    val stripeBD = BankDetails.stripe(customer.getId())
    val stripeCharge = charge(stripeBD, req.charge)
    Future.successful((stripeCharge.getId, stripeBD, req.charge))
  }

}



