package com.clemble.loveit.payment.service

import java.math.MathContext
import javax.inject.{Inject, Singleton}

import com.braintreegateway.{BraintreeGateway, Transaction, TransactionRequest}
import com.clemble.loveit.common.util.IDGenerator
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

trait BraintreeProcessingService extends PaymentProcessingService[BraintreePaymentRequest] {

  def generateToken(): Future[String]

}

@Singleton
case class SimpleBraintreeProcessingService @Inject()(gateway: BraintreeGateway) extends BraintreeProcessingService {

  override def generateToken(): Future[String] = {
    Future.successful(gateway.clientToken().generate())
  }

  private def createSaleRequest(paymentNonce: String, money: Money): TransactionRequest = {
    new TransactionRequest().
      orderId(IDGenerator.generate()).
      amount(money.amount.bigDecimal).
      merchantAccountId(money.currency.getCurrencyCode).
      paymentMethodNonce(paymentNonce).
      descriptor().name("CLMBLTD*Gratefull").
      done()
  }

  private def createSaleTransaction(req: BraintreePaymentRequest): Transaction = {
    val request = createSaleRequest(req.nonce, req.charge)
    val saleResult = gateway.transaction().sale(request)

    if (!saleResult.isSuccess())
      throw new IllegalArgumentException("Failed to process transaction")
    saleResult.getTarget()
  }

  override def process(req: BraintreePaymentRequest): Future[(String, BankDetails, Money)] = {
    val saleTransaction = createSaleTransaction(req)

    val bankDetails = BankDetails from saleTransaction.getPayPalDetails
    val money = Money from saleTransaction

    Future.successful((saleTransaction.getId, bankDetails, money))
  }

}

/**
  * Stipe processing service
  */
trait StripeProcessingService extends PaymentProcessingService[StripePaymentRequest]

import com.stripe.model.Charge
import com.stripe.model.Customer

@Singleton
case object JavaClientStripeProcessingService extends StripeProcessingService {

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

  override def process(req: StripePaymentRequest): Future[(String, BankDetails, Money)] = {
    val customer = createCustomer(req.token)
    val stripeBD = BankDetails.stripe(customer.getId())
    val stripeCharge = charge(stripeBD, req.charge)
    Future.successful((stripeCharge.getId, stripeBD, req.charge))
  }

}



