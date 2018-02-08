package com.clemble.loveit.test

import java.time.{LocalDateTime, LocalDate, YearMonth}
import java.util.Currency

import com.clemble.loveit.auth.model.requests.RegisterRequest
import com.clemble.loveit.common.error.{RepositoryException, ThankException, UserException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.{IDGenerator, LoveItCurrency}
import com.clemble.loveit.payment.model._
import com.clemble.loveit.thank.model._
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomStringUtils.{random, randomAlphabetic, randomNumeric}
import org.apache.commons.lang3.RandomUtils.{nextInt, nextLong}

import scala.collection.immutable.List
import scala.util.Random

package object util {

  implicit val booleanGenerator: Generator[Boolean] = () => Random.nextBoolean()
  implicit val intGenerator: Generator[Int] = () => Random.nextInt()

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
      List(someRandom[PendingTransaction])
    )
  }
  implicit val repositoryExceptionGenerator: Generator[RepositoryException] = () => {
    new RepositoryException(Random.nextString(10), Random.nextString(10))
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
  implicit val thankTransactionGenerator: Generator[ThankEvent] = () => {
    ThankEvent(someRandom[UserID], someRandom[SupportedProject], someRandom[Resource], someRandom[LocalDateTime])
  }
  implicit val supportedProjectGenerator: Generator[SupportedProject] = () => {
    SupportedProject(
      someRandom[Resource],
      someRandom[UserID],
      optionRandom[String],
      optionRandom[String],
      optionRandom[String],
      someRandom[Set[Tag]]
    )
  }
  implicit val pendingTransactionGenerator: Generator[PendingTransaction] = () => {
    PendingTransaction(someRandom[SupportedProject], someRandom[Resource], someRandom[LocalDateTime])
  }
  implicit val verificationGenerator: Generator[ROVerification[Resource]] = () => {
    val resource = someRandom[Resource]
    ROVerification(
      Pending,
      resource,
      RandomStringUtils.randomNumeric(10)
    )
  }
  implicit val postGenerator: Generator[Post] = () => {
    val resource = optionRandom[String] match {
      case None => someRandom[Resource]
      case Some(child) => Resource.from(s"${someRandom[Resource].stringify()}/${child}")
    }
    Post(
      resource,
      someRandom[SupportedProject].copy(resource = resource),
      someRandom[OpenGraphObject]
    )
  }
  implicit val thankGenerator: Generator[Thank] = () => Thank(someRandom[Long])

  implicit val userGenerator: Generator[User] = () => {
    User(
      id = random(10),
      firstName = optionRandom[String],
      lastName = optionRandom[String],
      email = s"${someRandom[String]}@${someRandom[String]}.${someRandom[String]}",
      avatar = optionRandom[String],
      dateOfBirth = optionRandom[LocalDate]
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
  implicit val dateGenerator: Generator[LocalDate] = () => {
    LocalDate.now()
  }
  implicit val yomGenerator: Generator[YearMonth] = () => {
    YearMonth.of(nextInt(1000, 3000), nextInt(1, 13))
  }
  implicit val currencyGenerator: Generator[Currency] = () => {
    LoveItCurrency.getInstance("USD")
  }

  implicit val longGenerator: Generator[Long] = () => nextLong(0, Long.MaxValue)

  implicit val ogiGenerator: Generator[OpenGraphImage] = () => OpenGraphImage(
    url = someRandom[Resource].stringify(),
    secureUrl = optionRandom[Resource].map(_.stringify()),
    imageType = optionRandom[MimeType],
    width = optionRandom[Int],
    height = optionRandom[Int],
    alt = optionRandom[String]
  )

  implicit val ogoGenerator: Generator[OpenGraphObject] = () => OpenGraphObject(
    url = someRandom[Resource].stringify(),
    title = optionRandom[String],
    image = optionRandom[OpenGraphImage],
    description = optionRandom[String],
    tags = someRandom[Set[Tag]],
  )

  implicit def setGenerator[T](implicit gen: Generator[T]): Generator[Set[T]] = () => {
    if (someRandom[Boolean]) {
      Set(gen.generate()) ++ someRandom[Set[T]]
    } else {
      Set(gen.generate())
    }
  }

  def optionRandom[T](implicit get: Generator[T]) = {
    if (someRandom[Boolean]) {
      Some(get.generate())
    } else {
      None
    }
  }

  def someRandom[T](implicit gen: Generator[T]) = gen.generate()

}
