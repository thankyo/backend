package com.clemble.loveit.test

import java.time.YearMonth
import java.util.Currency

import com.clemble.loveit.common.error.{RepositoryException, ThankException, UserException}
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.common.util.{IDGenerator, LoveItCurrency}
import com.clemble.loveit.payment.model._
import com.clemble.loveit.thank.model.{Pending, ROVerification, Thank}
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.{random, randomAlphabetic, randomNumeric}
import org.apache.commons.lang3.RandomUtils.{nextInt, nextLong}
import org.joda.time.DateTime

import scala.util.Random

package object util {

  implicit val resourceGenerator: Generator[Resource] = ResourceGenerator
  implicit val commonSocialProfileGenerator: Generator[CommonSocialProfile] = CommonSocialProfileGenerator
  implicit val paymentTransactionGenerator: Generator[EOMCharge] = PaymentTransactionGenerator
  implicit val repositoryExceptionGenerator: Generator[RepositoryException] = RepositoryExceptionGenerator
  implicit val thankExceptionGenerator: Generator[ThankException] = ThankExceptionGenerator
  implicit val userExceptionGenerator: Generator[UserException] = UserExceptionGenerator
  implicit val bankDetailsGenerator: Generator[BankDetails] = BankDetailsGenerator
  implicit val thankTransactionGenerator: Generator[ThankTransaction] = ThankTransactionGenerator
  implicit val verificationGenerator: Generator[ROVerification[Resource]] = ROVerificationGenerator
  implicit val thankGenerator: Generator[Thank] = ThankGenerator
  implicit val userGenerator: Generator[User] = UserGenerator
  implicit val userIDGenerator: Generator[UserID] = UserIDGenerator
  implicit val payoutGenerator: Generator[EOMPayout] = EOMPayoutGenerator
  implicit val eomStatGenerator: Generator[EOMStatistics] = EndOfMonthStatisticsGenerator
  implicit val eomProcGenerator: Generator[EOMStatus] = EOMStatusGenerator
  implicit val moneyGenerator: Generator[Money] = MoneyGenerator
  implicit val dateTimeGenerator: Generator[DateTime] = DateTimeGenerator
  implicit val yomGenerator: Generator[YearMonth] = YearMonthGenerator
  implicit val currencyGenerator: Generator[Currency] = CurrencyGenerator

  def someRandom[T](implicit gen: Generator[T]) = gen.generate()

  private object CurrencyGenerator extends Generator[Currency] {
    val generate: Currency = LoveItCurrency.getInstance("USD")
  }

  private object DateTimeGenerator extends Generator[DateTime] {
    override def generate(): DateTime = new DateTime(Random.nextLong())
  }

  private object YearMonthGenerator extends Generator[YearMonth] {
    override def generate(): YearMonth = YearMonth.of(nextInt(1000, 3000), nextInt(1, 13))
  }

  private object UserIDGenerator extends Generator[UserID] {
    override def generate(): UserID = IDGenerator.generate()
  }

  private object MoneyGenerator extends Generator[Money] {
    override def generate(): Money = {
      Money(nextLong(0, Long.MaxValue), someRandom[Currency])
    }
  }

  private object EOMStatusGenerator extends Generator[EOMStatus] {
    override def generate(): EOMStatus = {
      EOMStatus(
        someRandom[YearMonth],
        someRandom[EOMStatistics],
        someRandom[EOMStatistics],
        someRandom[EOMStatistics],
        someRandom[EOMStatistics],
        None,
        new DateTime(nextLong(0, Long.MaxValue))
      )
    }
  }

  private object EndOfMonthStatisticsGenerator extends Generator[EOMStatistics] {
    override def generate(): EOMStatistics = {
      EOMStatistics(
        nextLong(0, Long.MaxValue),
        nextLong(0, Long.MaxValue),
        nextLong(0, Long.MaxValue),
        nextLong(0, Long.MaxValue)
      )
    }
  }

  private object EOMPayoutGenerator extends Generator[EOMPayout] {
    override def generate(): EOMPayout = EOMPayout(
      someRandom[UserID],
      someRandom[YearMonth],
      Some(someRandom[BankDetails]),
      someRandom[Money],
      PayoutStatus.Pending
    )
  }

  private object BankDetailsGenerator extends Generator[BankDetails] {

    override def generate(): BankDetails = {
        StripeBankDetails(randomNumeric(10), Some(randomNumeric(4)), Some(randomNumeric(4)))
    }

  }

  private object CommonSocialProfileGenerator extends Generator[CommonSocialProfile] {

    override def generate(): CommonSocialProfile = {
      CommonSocialProfile(
        loginInfo = LoginInfo("test", RandomStringUtils.random(10)),
        firstName = Some(random(10)),
        lastName = Some(random(10))
      )
    }

  }

  private object PaymentTransactionGenerator extends Generator[EOMCharge] {

    override def generate(): EOMCharge = {
      EOMCharge(
        someRandom[UserID],
        someRandom[YearMonth],
        someRandom[BankDetails],
        ChargeStatus.Pending,
        someRandom[Money],
        None,
        List(someRandom[ThankTransaction])
      )
    }

  }

  private object RepositoryExceptionGenerator extends Generator[RepositoryException] {

    override def generate(): RepositoryException = new RepositoryException(RandomStringUtils.randomNumeric(10), RandomStringUtils.randomNumeric(30))

  }

  private object ResourceGenerator extends Generator[Resource] {

    override def generate(): Resource = {
      HttpResource(s"${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}")
    }

  }

  private object ROVerificationGenerator extends Generator[ROVerification[Resource]] {

    override def generate(): ROVerification[Resource] = {
      val resource = someRandom[Resource]
      ROVerification(
        Pending,
        resource,
        RandomStringUtils.randomNumeric(10)
      )
    }

  }

  private object ThankExceptionGenerator extends Generator[ThankException] {

    override def generate(): ThankException = {
      if (Random.nextBoolean())
        someRandom[RepositoryException]
      else
        someRandom[UserException]
    }

  }

  private object ThankGenerator extends Generator[Thank] {

    override def generate(): Thank = {
      Thank(
        someRandom[Resource],
        IDGenerator.generate(),
        nextLong(0, Long.MaxValue)
      )
    }

  }

  private object ThankTransactionGenerator extends Generator[ThankTransaction] {

    override def generate(): ThankTransaction = {
      ThankTransaction(someRandom[UserID], someRandom[UserID], someRandom[Resource], new DateTime(nextLong(0, Long.MaxValue)))
    }

  }

  private object UserExceptionGenerator extends Generator[UserException] {

    override def generate(): UserException = UserException.notEnoughFunds()

  }

  private object UserGenerator extends Generator[User] {

    override def generate(): User = {
      User(
        id = random(10),
        firstName = Some(random(10)),
        lastName = Some(random(10)),
        owns = Set.empty,
        balance = 200L,
        total = 200L,
        chargeAccount = Some(someRandom[BankDetails]),
        thumbnail = Some(random(12)),
        dateOfBirth = Some(new DateTime(nextLong(0, Long.MaxValue))),
        profiles = Set.empty[LoginInfo]
      )
    }

    def generate(ownership: Resource): User = {
      generate().copy(owns = Set(ownership))
    }

  }

}
