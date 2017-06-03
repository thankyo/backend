package com.clemble.loveit.test

import com.clemble.loveit.common.error.{RepositoryError, RepositoryException, ThankException, UserException}
import com.clemble.loveit.common.model.{HttpResource, Resource}
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
  implicit val paymentTransactionGenerator: Generator[PaymentTransaction] = PaymentTransactionGenerator
  implicit val repositoryErrorGenerator: Generator[RepositoryError] = RepositoryErrorGenerator
  implicit val repositoryExceptionGenerator: Generator[RepositoryException] = RepositoryExceptionGenerator
  implicit val thankExceptionGenerator: Generator[ThankException] = ThankExceptionGenerator
  implicit val userExceptionGenerator: Generator[UserException] = UserExceptionGenerator
  implicit val bankDetailsGenerator: Generator[BankDetails] = BankDetailsGenerator
  implicit val paymentOperationGenerator: Generator[PaymentOperation] = PaymentOperationGenerator
  implicit val thankTransactionGenerator: Generator[ThankTransaction] = ThankTransactionGenerator
  implicit val verificationGenerator: Generator[ROVerification[Resource]] = ROVerificationGenerator
  implicit val thankGenerator: Generator[Thank] = ThankGenerator
  implicit val userGenerator: Generator[User] = UserGenerator

  def someRandom[T](implicit gen: Generator[T]) = gen.generate()

  private object BankDetailsGenerator extends Generator[BankDetails] {

    override def generate(): BankDetails = {
      if (nextInt(0, 1) == 0)
        BankDetails.payPal(randomNumeric(10))
      else if (nextInt(0, 1) == 0)
        BankDetails.empty
      else
        BankDetails.stripe(randomNumeric(10))
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

  private object PaymentOperationGenerator extends Generator[PaymentOperation] {

    override def generate(): PaymentOperation = {
      if (nextInt(0, 1) == 0)
        Debit
      else
        Credit
    }

  }

  private object PaymentTransactionGenerator extends Generator[PaymentTransaction] {

    override def generate(): PaymentTransaction = {
      if (Random.nextBoolean()) {
        PaymentTransaction.debit(IDGenerator.generate(), someRandom[User].id, Random.nextLong(), Money(Random.nextLong(), LoveItCurrency.getInstance("USD")), someRandom[BankDetails])
      } else {
        PaymentTransaction.credit(IDGenerator.generate(), someRandom[User].id, Random.nextLong(), Money(Random.nextLong(), LoveItCurrency.getInstance("USD")), someRandom[BankDetails])
      }
    }

  }

  private object RepositoryErrorGenerator extends Generator[RepositoryError] {

    override def generate(): RepositoryError = RepositoryError(random(10), random(20))

  }

  private object RepositoryExceptionGenerator extends Generator[RepositoryException] {

    override def generate(): RepositoryException = new RepositoryException(someRandom[RepositoryError])

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
      if (nextInt(0, 1) == 0)
        ThankTransaction.debit(someRandom[User].id, someRandom[Resource], nextLong(0, Long.MaxValue))
      else
        ThankTransaction.credit(someRandom[User].id, someRandom[Resource], nextLong(0, Long.MaxValue))
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
        bankDetails = someRandom[BankDetails],
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
