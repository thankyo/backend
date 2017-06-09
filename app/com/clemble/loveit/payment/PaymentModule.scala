package com.clemble.loveit.payment

import java.util.Currency

import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.payment.service.repository.{BalanceRepository, _}
import com.clemble.loveit.payment.service.repository.mongo._
import com.clemble.loveit.payment.service._
import com.clemble.loveit.common.util.LoveItCurrency
import javax.inject.{Named, Singleton}

import com.google.inject.Provides
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

    bind[BalanceRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[BankDetailsRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[MonthlyLimitRepository].to[MongoPaymentRepository].asEagerSingleton()
    bind[PaymentRepository].to[MongoPaymentRepository].asEagerSingleton()

    bind[EOMStatusRepository].to[MongoEOMStatusRepository].asEagerSingleton()

    bind[BankDetailsService].to[SimpleBankDetailsService].asEagerSingleton()
    bind[UserPaymentRepository].to[MongoUserPaymentRepository].asEagerSingleton()

    val currencyToAmount: Map[Currency, Amount] = Map[Currency, Amount](LoveItCurrency.getInstance("USD") -> 10L)
    bind[ExchangeService].toInstance(InMemoryExchangeService(currencyToAmount))

    bind(classOf[ThankTransactionService]).to(classOf[SimpleThankTransactionService])
    bind(classOf[ThankTransactionRepository]).to(classOf[MongoThankTransactionRepository])
  }

  @Provides
  @Singleton
  def bankDetailsService(configuration: Configuration): BankDetailsConverter = {
    new StripeBankDetailsConverter(configuration.getString("payment.stripe.apiKey").get)
  }

  @Provides
  @Singleton
  def payoutService(): PayoutService[BankDetails] = {
    StripePayoutService
  }

  @Provides
  @Singleton
  @Named("eomCharge")
  def eomChargeMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("eomCharge", FailoverStrategy.default))(ec)
    Await.result(fCollection, 1 minute)
  }

  @Provides
  @Singleton
  @Named("eomStatus")
  def eomStatusMongoCollection(mongoApi: ReactiveMongoApi, ec: ExecutionContext): JSONCollection = {
    val fCollection: Future[JSONCollection] = mongoApi.
      database.
      map(_.collection[JSONCollection]("eomStatus", FailoverStrategy.default))(ec)
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
