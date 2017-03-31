package com.clemble.loveit.payment.service

import com.braintreegateway.{BraintreeGateway, Transaction, TransactionRequest}
import com.clemble.loveit.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, Money, PaymentTransaction}
import com.clemble.loveit.util.IDGenerator
import com.google.inject.Inject
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future

trait BraintreeService {

  def generateToken(): Future[String]

  def processNonce(userID: UserID, nonce: String, money: Money): Future[PaymentTransaction]

}

case class SimpleBraintreeService @Inject()(gateway: BraintreeGateway, paymentService: PaymentTransactionService) extends BraintreeService {

  override def generateToken(): Future[String] = {
    Future.successful(gateway.clientToken().generate())
  }

  private def createSaleRequest(paymentNonce: String, money: Money): TransactionRequest = {
    new TransactionRequest().
      amount(money.amount.bigDecimal).
      merchantAccountId(money.currency.getCurrencyCode).
      paymentMethodNonce(paymentNonce).
      orderId(IDGenerator.generate()).
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

  override def processNonce(userID: UserID, paymentNonce: String, amount: Money): Future[PaymentTransaction] = {
    val saleTransaction = createSaleTransaction(paymentNonce, amount)

    val bankDetails: BankDetails = BankDetails from saleTransaction.getPayPalDetails
    val money = Money from saleTransaction
    paymentService.receive(userID, bankDetails, money, saleTransaction)
  }

}
