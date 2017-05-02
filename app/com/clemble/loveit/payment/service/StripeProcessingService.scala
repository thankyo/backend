package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model._
import com.google.common.collect.Maps

import scala.concurrent.Future

/**
  * Stipe processing service
  */
trait StripeProcessingService extends PaymentProcessingService[StripePaymentRequest]

import com.stripe.model.Charge
import com.stripe.model.Customer

@Singleton
case class JavaClientStripeProcessingService @Inject()(
                                               apiKey: String
                                             ) extends StripeProcessingService {

  def charge(bankDetails: StripeBankDetails, amount: Money): Charge = {
    val chargeParams = Maps.newHashMap[String, Object]()
    chargeParams.put("amount", amount.amount)
    chargeParams.put("currency", amount.currency.getCurrencyCode.toLowerCase())
    chargeParams.put("customer", bankDetails.customer)
    Charge.create(chargeParams)
  }

  def createCustomer(token: String): Customer = {
    val customerParams = Maps.newHashMap[String, Object]()
    customerParams.put("email", "paying.user@example.com")
    customerParams.put("source", token)
    Customer.create(customerParams)
  }

  override def process(req: StripePaymentRequest): Future[(String, BankDetails, Money)] = {
    val customer = createCustomer(req.nonce)
    val stripeBD = BankDetails.stripe(customer.getId())
    val stripeCharge = charge(stripeBD, req.money)
    Future.successful((stripeCharge.getId, stripeBD, req.money))
  }

}