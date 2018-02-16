package com.clemble.loveit.payment

import java.util.Currency

import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.payment.service.repository.{_}
import com.clemble.loveit.payment.service.repository.mongo._
import com.clemble.loveit.payment.service._
import com.clemble.loveit.common.util.LoveItCurrency
import javax.inject.{Named, Singleton}

import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.stripe.Stripe
import com.stripe.net.RequestOptions
import com.stripe.net.RequestOptions.RequestOptionsBuilder
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext

case class PaymentModule(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure() = {
    val apiKey = conf.get[String]("payment.stripe.apiKey")
    val clientId = conf.get[String]("payment.stripe.clientId")
    Stripe.apiKey = apiKey
    Stripe.clientId = clientId


    bind[EOMChargeRepository].to[MongoEOMChargeRepository]

    bind[UserPaymentRepository].to[MongoPaymentRepository].asEagerSingleton()

    bind[ChargeAccountRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[ChargeAccountConverter].to[StripeChargeAccountConverter].asEagerSingleton()
    bind[ChargeAccountService].to[SimpleChargeAccountService].asEagerSingleton()

    bind[PayoutAccountRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PayoutAccountConverter].to[StripePayoutAccountConverter].asEagerSingleton()
    bind[PayoutAccountService].to[SimplePayoutAccountService].asEagerSingleton()

    bind[PaymentLimitRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PaymentRepository].to[MongoPaymentRepository].asEagerSingleton()

    bind[UserPaymentService].to[SimpleUserPaymentService].asEagerSingleton()

    bind[EOMPaymentService].to[SimpleEOMPaymentService].asEagerSingleton()
    bind[EOMChargeService].to(classOf[StripeEOMChargeService])
    bind[EOMStatusRepository].to[MongoEOMStatusRepository].asEagerSingleton()
    bind[EOMPayoutRepository].to[MongoEOMPayoutRepository].asEagerSingleton()

    bind[ChargeAccountService].to[SimpleChargeAccountService].asEagerSingleton()

    val currencyToAmount: Map[Currency, Amount] = Map[Currency, Amount](LoveItCurrency.getInstance("USD") -> 10L)
    bind[ExchangeService].toInstance(InMemoryExchangeService(currencyToAmount))

    bind(classOf[PendingTransactionService]).to(classOf[SimplePendingTransactionService]).asEagerSingleton()
    bind(classOf[PendingTransactionRepository]).to(classOf[MongoPendingTransactionRepository])
  }

  @Provides
  @Singleton
  def requestOptions(): RequestOptions = {
    (new RequestOptionsBuilder()).
      setApiKey(conf.get[String]("payment.stripe.apiKey")).
      build()
  }

  @Provides
  @Singleton
  def payoutService(): EOMPayoutService = {
    StripeEOMPayoutService
  }

  @Provides
  @Singleton
  @Named("eomCharge")
  def eomChargeMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("eomCharge", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("eomPayout")
  def eomPayoutMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("eomPayout", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("eomStatus")
  def eomStatusMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("eomStatus", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("userPayment")
  def userPaymentCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("userPayment", mongoApi, ec, env)
  }


  @Provides
  @Singleton
  @Named("thankTransactions")
  def thankTransactionMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("thankTransaction", mongoApi, ec, env)
  }

  @Provides
  @Singleton
  @Named("paymentCrypter")
  def paymentCrypter(): Crypter = {
    val key = conf.get[String]("payment.crypter.key")
    val config = JcaCrypterSettings(key)
    new JcaCrypter(config)
  }

}
