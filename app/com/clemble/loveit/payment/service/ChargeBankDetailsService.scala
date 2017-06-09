package com.clemble.loveit.payment.service

import javax.inject.{Singleton}

import com.clemble.loveit.payment.model._
import com.google.common.collect.Maps

import scala.concurrent.Future

/**
  * Payment processing service abstraction
  */
sealed trait ChargeBankDetailsService[T <: BankDetails] {

  /**
    * Process payment request by the user
    */
  def process(token: String): Future[T]

}

import com.stripe.model.Customer

/**
  * Stripe processing service
  */
@Singleton
case object StripeProcessingService extends ChargeBankDetailsService[StripeBankDetails] {

  override def process(token: String): Future[StripeBankDetails] = {
    val customerParams = Maps.newHashMap[String, Object]()
    customerParams.put("source", token)

    val customer = Customer.create(customerParams)

    val bankDetails = BankDetails.stripe(customer.getId())
    Future.successful(bankDetails)
  }

}



