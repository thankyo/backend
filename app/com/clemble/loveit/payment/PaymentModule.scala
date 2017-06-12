package com.clemble.loveit.payment

import java.util.Currency

import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.payment.model.ChargeAccount
import com.clemble.loveit.payment.service.repository.{BalanceRepository, _}
import com.clemble.loveit.payment.service.repository.mongo._
import com.clemble.loveit.payment.service._
import com.clemble.loveit.common.util.LoveItCurrency
import javax.inject.{Named, Singleton}

import com.clemble.loveit.common.mongo.JSONCollectionFactory
import com.google.inject.Provides
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext}

case class PaymentModule(env: Environment, conf: Configuration) extends ScalaModule {

  override def configure() = {
    bind[EOMChargeRepository].to[MongoEOMChargeRepository]

    bind[BalanceRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PaymentAccountRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[MonthlyLimitRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PaymentRepository].to[MongoPaymentRepository].asEagerSingleton()

    bind[EOMService].to[SimpleEOMService].asEagerSingleton()
    bind[EOMChargeService].toInstance(StripeEOMChargeService)
    bind[EOMStatusRepository].to[MongoEOMStatusRepository].asEagerSingleton()
    bind[EOMPayoutRepository].to[MongoEOMPayoutRepository].asEagerSingleton()

    bind[PaymentAccountService].to[SimplePaymentAccountService].asEagerSingleton()
    bind[UserPaymentRepository].to[MongoUserPaymentRepository].asEagerSingleton()

    val currencyToAmount: Map[Currency, Amount] = Map[Currency, Amount](LoveItCurrency.getInstance("USD") -> 10L)
    bind[ExchangeService].toInstance(InMemoryExchangeService(currencyToAmount))

    bind(classOf[ThankTransactionService]).to(classOf[SimpleThankTransactionService])
    bind(classOf[ThankTransactionRepository]).to(classOf[MongoThankTransactionRepository])
  }

  @Provides
  @Singleton
  def chargeAccountService(): ChargeAccountConverter = {
    new StripeChargeAccountConverter(conf.getString("payment.stripe.apiKey").get)
  }

  @Provides
  @Singleton
  def payoutService(): EOMPayoutService[ChargeAccount] = {
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
  @Named("thankTransactions")
  def thankTransactionMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    JSONCollectionFactory.create("thankTransaction", mongoApi, ec, env)
  }

}
