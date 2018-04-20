package com.clemble.loveit.payment

import java.util.Currency

import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.payment.service.repository._
import com.clemble.loveit.payment.service.repository.mongo._
import com.clemble.loveit.payment.service.{StripePayoutAccountConverter, _}
import com.clemble.loveit.common.util.LoveItCurrency
import javax.inject.{Named, Singleton}
import com.clemble.loveit.common.mongo.{JSONCollectionFactory}
import com.google.inject.Provides
import com.mohiva.play.silhouette.api.crypto.Crypter
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings}
import com.stripe.Stripe
import com.stripe.net.RequestOptions
import com.stripe.net.RequestOptions.RequestOptionsBuilder
import net.codingwell.scalaguice.ScalaModule
import play.api.libs.ws.WSClient
import play.api.{Configuration, Environment, Mode}
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
    bind[ChargeAccountService].to[SimpleChargeAccountService].asEagerSingleton()

    bind[PayoutAccountRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PayoutAccountService].to[SimplePayoutAccountService].asEagerSingleton()

    bind[PaymentLimitRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PaymentRepository].to[MongoPaymentRepository].asEagerSingleton()

    bind[UserPaymentService].to[SimpleUserPaymentService].asEagerSingleton()

    bind[EOMPayoutService].to(classOf[SimpleEOMPayoutService])
    bind[EOMChargeService].to(classOf[SimpleEOMChargeService])

    bind[EOMPaymentService].to[SimpleEOMPaymentService].asEagerSingleton()
    bind[EOMStatusRepository].to[MongoEOMStatusRepository].asEagerSingleton()
    bind[EOMPayoutRepository].to[MongoEOMPayoutRepository].asEagerSingleton()

    bind[ChargeAccountService].to[SimpleChargeAccountService].asEagerSingleton()

    val currencyToAmount: Map[Currency, Amount] = Map[Currency, Amount](LoveItCurrency.getInstance("USD") -> 10L)
    bind[ExchangeService].toInstance(InMemoryExchangeService(currencyToAmount))

    bind(classOf[PendingTransactionService]).to(classOf[SimplePendingTransactionService]).asEagerSingleton()
    bind(classOf[PendingTransactionRepository]).to(classOf[MongoPendingTransactionRepository])

    bind(classOf[ContributionStatisticsService]).to(classOf[SimpleContributionStatisticsService])
    bind(classOf[ContributionStatisticsRepository]).to(classOf[MongoContributionStatisticsRepository])
  }

  @Provides
  @Singleton
  def chargeAccountConverter(implicit ec: ExecutionContext): ChargeAccountConverter = {
    if (env.mode == Mode.Dev || env.mode == Mode.Test) {
      DevChargeAccountConverter
    } else {
      new StripeChargeAccountConverter()
    }
  }

  @Provides
  @Singleton
  def eomChargeProcessor(options: RequestOptions): EOMChargeProcessor = {
    if (env.mode == Mode.Dev || env.mode == Mode.Test) {
      DevEOMChargeProcessor
    } else {
      StripeEOMChargeProcessor(options)
    }
  }

  @Provides
  @Singleton
  def eomPayoutProcessor(): EOMPayoutProcessor = {
    if (env.mode == Mode.Dev || env.mode == Mode.Test) {
      DevEOMPayoutProcessor
    } else {
      StripeEOMPayoutProcessor
    }
  }

  @Provides
  @Singleton
  def payoutAccountConverter(wsClient: WSClient, ec: ExecutionContext): PayoutAccountConverter = {
    if (env.mode == Mode.Dev || env.mode == Mode.Test) {
      DevPayoutAccountConverter
    } else {
      new StripePayoutAccountConverter(wsClient, ec)
    }
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
  @Named("eomCharge")
  def eomChargeMongoCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("eomCharge")
  }

  @Provides
  @Singleton
  @Named("eomPayout")
  def eomPayoutMongoCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("eomPayout")
  }

  @Provides
  @Singleton
  @Named("eomStatus")
  def eomStatusMongoCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("eomStatus")
  }

  @Provides
  @Singleton
  @Named("userPayment")
  def userPaymentCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("userPayment")
  }


  @Provides
  @Singleton
  @Named("thankTransactions")
  def thankTransactionMongoCollection(collectionFactory: JSONCollectionFactory): JSONCollection = {
    collectionFactory.create("thankTransaction")
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
