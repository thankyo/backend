package com.clemble.loveit.payment

import java.util.Currency

import com.braintreegateway.BraintreeGateway
import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.payment.model.{BankDetails, PaymentRequest}
import com.clemble.loveit.payment.service.repository.{PaymentRepository, EOMChargeRepository, ThankTransactionRepository, UserPaymentRepository}
import com.clemble.loveit.payment.service.repository.mongo.{MongoPaymentRepository, MongoEOMChargeRepository, MongoThankTransactionRepository, MongoUserPaymentRepository}
import com.clemble.loveit.payment.service._
import com.clemble.loveit.common.util.LoveItCurrency
import javax.inject.{Named, Singleton}

import com.google.inject.Provides
import com.paypal.base.rest.APIContext
import com.stripe.Stripe
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.FailoverStrategy
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

class PaymentModule extends ScalaModule {

  override def configure() = {
    bind[EOMChargeRepository].to[MongoEOMChargeRepository]

    bind[PaymentRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[UserPaymentRepository].to[MongoUserPaymentRepository].asEagerSingleton()

    val currencyToAmount: Map[Currency, Amount] = Map[Currency, Amount](LoveItCurrency.getInstance("USD") -> 10L)
    bind[ExchangeService].toInstance(InMemoryExchangeService(currencyToAmount))

    bind(classOf[ThankTransactionService]).to(classOf[SimpleThankTransactionService])
    bind(classOf[ThankTransactionRepository]).to(classOf[MongoThankTransactionRepository])
  }

  @Provides
  @Singleton
  def processingService(configuration: Configuration): PaymentProcessingService[PaymentRequest] = {
    Stripe.apiKey = configuration.getString("payment.stripe.apiKey").get
    StripeProcessingService
  }

  @Provides
  @Singleton
  def apiContext(configuration: Configuration): APIContext = {
    val clientId = configuration.getString("payment.payPal.rest.clientId").get
    val clientSecret = configuration.getString("payment.payPal.rest.clientSecret").get
    val mode = configuration.getString("payment.payPal.rest.mode").get
    new APIContext(clientId, clientSecret, mode)
  }

  @Provides
  @Singleton
  def payoutService(): PayoutService[BankDetails] = {
    StripePayoutService
  }

  @Provides
  @Singleton
  @Named("eomCharge")
  def paymentTransactionMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("eomCharge", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

  @Provides
  @Singleton
  @Named("thankTransactions")
  def thankTransactionMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("thankTransaction", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

}
