package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.PaymentException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{ChargeAccount, StripeCustomerToken}
import com.clemble.loveit.payment.service.repository.ChargeAccountRepository
import com.google.common.collect.ImmutableMap
import com.stripe.model.{Card, Customer}
import play.api.libs.ws.WSClient

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

/**
  * [[ChargeAccount]] integration service
  */
trait ChargeAccountService {

  def getChargeAccount(user: UserID): Future[Option[ChargeAccount]]

  def updateChargeAccount(user: UserID, token: StripeCustomerToken): Future[ChargeAccount]

  def deleteChargeAccount(user: UserID): Future[Boolean]

}

@Singleton
case class SimpleChargeAccountService @Inject()(repo: ChargeAccountRepository, converter: ChargeAccountConverter, implicit val ec: ExecutionContext) extends ChargeAccountService {


  override def getChargeAccount(user: UserID): Future[Option[ChargeAccount]] = {
    repo.getChargeAccount(user)
  }

  override def updateChargeAccount(user: UserID, token: StripeCustomerToken): Future[ChargeAccount] = {
    for {
      chAcc <- converter.processChargeToken(token)
      updated <- repo.setChargeAccount(user, chAcc)
    } yield {
      if (!updated) throw PaymentException.failedToLinkChargeAccount(user)
      chAcc
    }
  }

  override def deleteChargeAccount(user: UserID) = {
    for {
      customerOpt <- repo.getChargeAccount(user)
      removed <- repo.deleteChargeAccount(user)
    } yield {
      if (customerOpt.isDefined) {
        Customer.retrieve(customerOpt.get.customer).delete()
      }
      removed
    }
  }
}


/**
  * Payment processing service abstraction
  */
sealed trait ChargeAccountConverter {

  /**
    * Process payment request by the user
    */
  def processChargeToken(token: String): Future[ChargeAccount]

}


/**
  * Stripe processing service
  */
@Singleton
class StripeChargeAccountConverter @Inject() (implicit val ec: ExecutionContext) extends ChargeAccountConverter {
  import com.stripe.model.Customer

  private def toInternalChargeAccount(customer: Customer): ChargeAccount = {
    val (brand, last4) = asScalaBuffer(customer.getSources.getData).
      collectFirst({ case card: Card => (Option(card.getBrand), Option(card.getLast4))}).
      getOrElse(None -> None)
    ChargeAccount(customer.getId, brand, last4)
  }

  override def processChargeToken(token: String): Future[ChargeAccount] = {
    // Step 1. Generating customer params
    val customerParams = ImmutableMap.of[String, Object]("source", token)
    // Step 2. Generating customer
    val customer = Customer.create(customerParams)
    // Step 3. Converting to  Stripe customer with customer id
    val chargeAccount = toInternalChargeAccount(customer)
    // Step 4. Generating successful request
    Future.successful(chargeAccount)
  }

}