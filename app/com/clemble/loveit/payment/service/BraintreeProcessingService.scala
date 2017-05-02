package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import com.braintreegateway.{BraintreeGateway, Transaction, TransactionRequest}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.payment.model._

import scala.concurrent.Future

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
    val request = createSaleRequest(req.nonce, req.money)
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
