package com.clemble.loveit.payment.service

import com.braintreegateway.{BraintreeGateway, Transaction, TransactionRequest}
import com.clemble.loveit.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, BraintreeRequest, Money, PaymentTransaction}
import com.clemble.loveit.util.IDGenerator
import com.google.inject.Inject
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

trait BraintreeService {

  def generateToken(): Future[String]

  def process(user: UserID, req: BraintreeRequest): Future[PaymentTransaction] = {
    processNonce(user, req.nonce, req.money)
  }

  def processNonce(userID: UserID, nonce: String, money: Money): Future[PaymentTransaction]

}

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

  private def createSaleTransaction(paymentNonce: String, money: Money): Transaction = {
    val request = createSaleRequest(paymentNonce, money)
    val saleResult = gateway.transaction().sale(request)

    if (!saleResult.isSuccess())
      throw new IllegalArgumentException("Failed to process transaction")
    saleResult.getTarget()
  }

  override def processNonce(user: UserID, paymentNonce: String, amount: Money): Future[PaymentTransaction] = {
    val saleTransaction = createSaleTransaction(paymentNonce, amount)

    val bankDetails = BankDetails from saleTransaction.getPayPalDetails
    val money = Money from saleTransaction

    val thanks = exchangeService.toThanks(money)
    val transaction = PaymentTransaction.debit(saleTransaction.getOrderId, user, thanks, money, bankDetails)

    paymentService.receive(transaction)
  }

}
