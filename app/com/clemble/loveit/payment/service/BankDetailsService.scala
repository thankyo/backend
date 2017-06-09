package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, StripeBankDetails, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository.PaymentRepository
import com.google.common.collect.{ImmutableMap, Maps}
import com.stripe.Stripe
import com.stripe.model.Card

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

/**
  * BankDetails integration service
  */
trait BankDetailsService {

  def getBankDetails(user: UserID): Future[Option[BankDetails]]

  def updateBankDetails(user: UserID, token: StripeCustomerToken): Future[BankDetails]

}

@Singleton
case class SimpleBankDetailsService @Inject()(repo: PaymentRepository, bankDetailsService: BankDetailsConverter, implicit val ec: ExecutionContext) extends BankDetailsService {


  override def getBankDetails(user: UserID): Future[Option[BankDetails]] = {
    repo.getBankDetails(user)
  }

  override def updateBankDetails(user: UserID, token: StripeCustomerToken): Future[BankDetails] = {
    for {
      bankDetails <- bankDetailsService.process(token)
      updated <- repo.setBankDetails(user, bankDetails)
    } yield {
      if (!updated) throw PaymentException.failedToLinkBankDetails(user)
      bankDetails
    }
  }

}


/**
  * Payment processing service abstraction
  */
sealed trait BankDetailsConverter {

  /**
    * Process payment request by the user
    */
  def process(token: String): Future[_ <: BankDetails]

}


/**
  * Stripe processing service
  */
@Singleton
class StripeBankDetailsConverter(apiKey: String) extends BankDetailsConverter {
  Stripe.apiKey = apiKey

  import com.stripe.model.Customer

  private def toInternalBankDetails(customer: Customer): StripeBankDetails = {
    val (brand, last4) = customer.
      getSources.
      getData().
      to[List].
      collectFirst({ case card: Card => (Option(card.getBrand), Option(card.getLast4))}).
      getOrElse(None -> None)
    StripeBankDetails(customer.getId, brand, last4)
  }

  override def process(token: String): Future[StripeBankDetails] = {
    // Step 1. Generating customer params
    val customerParams = ImmutableMap.of[String, Object]("source", token)
    // Step 2. Generating customer
    val customer = Customer.create(customerParams)
    // Step 3. Converting to  Stripe customer with customer id
    val bankDetails = toInternalBankDetails(customer)
    // Step 4. Generating successful request
    Future.successful(bankDetails)
  }

}