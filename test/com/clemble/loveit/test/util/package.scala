package com.clemble.loveit.test

import java.time.{LocalDateTime, YearMonth}
import java.util.Currency

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.error.{RepositoryException, ThankException, UserException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.{IDGenerator, LoveItCurrency}
import com.clemble.loveit.payment.model._
import com.clemble.loveit.thank.model.{Pending, ROVerification, Thank}
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.{random, randomAlphabetic, randomNumeric}
import org.apache.commons.lang3.RandomUtils.{nextInt, nextLong}

import scala.util.Random

package object util {

  implicit val booleanGenerator: Generator[Boolean] = () => Random.nextBoolean()
  implicit val resourceGenerator: Generator[Resource] = () => {
    HttpResource(s"${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}")
  }
  implicit val httpResourceGenerator: Generator[HttpResource] = () => {
    HttpResource(s"${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}")
  }
  implicit val commonSocialProfileGenerator: Generator[CommonSocialProfile] = () => {
    CommonSocialProfile(
      loginInfo = LoginInfo("test", RandomStringUtils.random(10)),
      firstName = Some(random(10)),
      lastName = Some(random(10))
    )
  }
  implicit val registerRequest: Generator[RegisterRequest] = () => {
    RegisterRequest(
      firstName = random(10),
      lastName = random(10),
      email = random(10),
      password = random(10)
    )
  }
  implicit val paymentTransactionGenerator: Generator[EOMCharge] = () => {
    EOMCharge(
      someRandom[UserID],
      someRandom[YearMonth],
      optionRandom[ChargeAccount],
      ChargeStatus.Pending,
      someRandom[Money],
      None,
      List(someRandom[ThankTransaction])
    )
  }
  implicit val repositoryExceptionGenerator: Generator[RepositoryException] = () => {
    new RepositoryException(RandomStringUtils.randomNumeric(10), RandomStringUtils.randomNumeric(30))
  }
  implicit val thankExceptionGenerator: Generator[ThankException] = () => {
    if (Random.nextBoolean())
      someRandom[RepositoryException]
    else
      someRandom[UserException]
  }

  implicit val userExceptionGenerator: Generator[UserException] = () => {
    UserException.notEnoughFunds()
  }
  implicit val chargeAccountGenerator: Generator[ChargeAccount] = () => {
    ChargeAccount(randomNumeric(10), Some(randomNumeric(4)), Some(randomNumeric(4)))
  }
  implicit val payoutAccountGenerator: Generator[PayoutAccount] = () => {
    PayoutAccount(randomNumeric(10), randomNumeric(4), randomNumeric(4))
  }
  implicit val thankTransactionGenerator: Generator[ThankTransaction] = () => {
    ThankTransaction(someRandom[UserID], someRandom[UserID], someRandom[Resource], someRandom[LocalDateTime])
  }
  implicit val verificationGenerator: Generator[ROVerification[Resource]] = () => {
    val resource = someRandom[Resource]
    ROVerification(
      Pending,
      resource,
      RandomStringUtils.randomNumeric(10)
    )
  }
  implicit val thankGenerator: Generator[Thank] = () => {
    Thank(
      someRandom[Resource],
      IDGenerator.generate(),
      nextLong(0, Long.MaxValue)
    )
  }
  implicit val userGenerator: Generator[User] = () => {
    User(
      id = random(10),
      firstName = optionRandom[String],
      lastName = optionRandom[String],
      email = s"${someRandom[String]}@${someRandom[String]}.${someRandom[String]}",
      avatar = optionRandom[String],
      dateOfBirth = optionRandom[LocalDateTime],
      profiles = Set.empty[LoginInfo]
    )
  }
  implicit val userIDGenerator: Generator[UserID] = () => IDGenerator.generate()
  implicit val payoutGenerator: Generator[EOMPayout] = () => {
    EOMPayout(
      someRandom[UserID],
      someRandom[YearMonth],
      optionRandom[PayoutAccount],
      someRandom[Money],
      PayoutStatus.Pending
    )
  }
  implicit val eomStatGenerator: Generator[EOMStatistics] = () => {
    EOMStatistics(
      nextLong(0, Long.MaxValue),
      nextLong(0, Long.MaxValue),
      nextLong(0, Long.MaxValue),
      nextLong(0, Long.MaxValue)
    )
  }
  implicit val eomProcGenerator: Generator[EOMStatus] = () => {
    EOMStatus(
      someRandom[YearMonth],
      optionRandom[EOMStatistics],
      optionRandom[EOMStatistics],
      optionRandom[EOMStatistics],
      optionRandom[EOMStatistics],
      optionRandom[LocalDateTime],
      someRandom[LocalDateTime]
    )
  }
  implicit val moneyGenerator: Generator[Money] = () => {
    Money(nextInt(0, 1000), someRandom[Currency])
  }
  implicit val dateTimeGenerator: Generator[LocalDateTime] = () => {
    LocalDateTime.now()
  }
  implicit val yomGenerator: Generator[YearMonth] = () => {
    YearMonth.of(nextInt(1000, 3000), nextInt(1, 13))
  }
  implicit val currencyGenerator: Generator[Currency] = () => {
    LoveItCurrency.getInstance("USD")
  }

  implicit val longGenerator: Generator[Long] = () => nextLong(0, Long.MaxValue)

  def optionRandom[T](implicit get: Generator[T]) = {
    if (someRandom[Boolean]) {
      Some(get.generate())
    } else {
      None
    }
  }

  def someRandom[T](implicit gen: Generator[T]) = gen.generate()

}
