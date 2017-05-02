package com.clemble.loveit.payment.service

import com.braintreegateway.{BraintreeGateway, Transaction, TransactionRequest}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, BraintreeRequest, Money, PaymentTransaction}
import com.clemble.loveit.common.util.IDGenerator
import com.google.inject.{Inject, Singleton}

import scala.concurrent.Future

trait BraintreeService {

  def generateToken(): Future[String]

  def processNonce(user: UserID, req: BraintreeRequest): Future[PaymentTransaction]

}

@Singleton
case class SimpleBraintreeService @Inject()(gateway: BraintreeGateway, paymentService: PaymentTransactionService, exchangeService: ExchangeService) extends BraintreeService {

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

  private def createSaleTransaction(req: BraintreeRequest): Transaction = {
    val request = createSaleRequest(req.nonce, req.money)
    val saleResult = gateway.transaction().sale(request)

    if (!saleResult.isSuccess())
      throw new IllegalArgumentException("Failed to process transaction")
    saleResult.getTarget()
  }

  override def processNonce(user: UserID, req: BraintreeRequest): Future[PaymentTransaction] = {
    val saleTransaction = createSaleTransaction(req)

    val bankDetails = BankDetails from saleTransaction.getPayPalDetails
    val money = Money from saleTransaction

    val thanks = exchangeService.toThanks(money)
    val transaction = PaymentTransaction.debit(saleTransaction.getOrderId, user, thanks, money, bankDetails)

    paymentService.receive(transaction)
  }

}
