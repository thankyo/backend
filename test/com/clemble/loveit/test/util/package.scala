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

  implicit val resourceGenerator = ResourceGenerator
  implicit val commonSocialProfileGenerator = CommonSocialProfileGenerator
  implicit val paymentTransactionGenerator = PaymentTransactionGenerator
  implicit val repositoryErrorGenerator = RepositoryErrorGenerator
  implicit val repositoryExceptionGenerator = RepositoryExceptionGenerator
  implicit val thankExceptionGenerator = ThankExceptionGenerator
  implicit val userExceptionGenerator = UserExceptionGenerator
  implicit val bankDetailsGenerator = BankDetailsGenerator
  implicit val paymentOperationGenerator = PaymentOperationGenerator
  implicit val thankTransactionGenerator = ThankTransactionGenerator
  implicit val verificationGenerator = ROVerificationGenerator
  implicit val thankGenerator = ThankGenerator
  implicit val userGenerator = UserGenerator

  def some[T](implicit gen: Generator[T]) = gen.generate()

  object BankDetailsGenerator extends Generator[BankDetails] {

    override def generate(): BankDetails = {
      if (nextInt(0, 1) == 0)
        BankDetails.payPal(randomNumeric(10))
      else if (nextInt(0, 1) == 0)
        BankDetails.empty
      else
        BankDetails.stripe(randomNumeric(10))
    }

  }

  object CommonSocialProfileGenerator extends Generator[CommonSocialProfile] {

    override def generate(): CommonSocialProfile = {
      CommonSocialProfile(
        loginInfo = LoginInfo("test", RandomStringUtils.random(10)),
        firstName = Some(random(10)),
        lastName = Some(random(10))
      )
    }

  }

  object PaymentOperationGenerator extends Generator[PaymentOperation] {

    override def generate(): PaymentOperation = {
      if (nextInt(0, 1) == 0)
        Debit
      else
        Credit
    }

  }

  object PaymentTransactionGenerator extends Generator[PaymentTransaction] {

    override def generate(): PaymentTransaction = {
      if (Random.nextBoolean()) {
        PaymentTransaction.debit(IDGenerator.generate(), some[User].id, Random.nextLong(), Money(Random.nextLong(), LoveItCurrency.getInstance("USD")), some[BankDetails])
      } else {
        PaymentTransaction.credit(IDGenerator.generate(), some[User].id, Random.nextLong(), Money(Random.nextLong(), LoveItCurrency.getInstance("USD")), some[BankDetails])
      }
    }

  }

  object RepositoryErrorGenerator extends Generator[RepositoryError] {

    override def generate(): RepositoryError = RepositoryError(random(10), random(20))

  }

  object RepositoryExceptionGenerator extends Generator[RepositoryException] {

    override def generate(): RepositoryException = new RepositoryException(some[RepositoryError])

  }

  object ResourceGenerator extends Generator[Resource] {

    override def generate(): Resource = {
      HttpResource(s"${randomAlphabetic(10)}.${randomAlphabetic(4)}/${randomAlphabetic(3)}/${randomAlphabetic(4)}")
    }

  }

  object ROVerificationGenerator extends Generator[ROVerification[Resource]] {

    override def generate(): ROVerification[Resource] = {
      val resource = some[Resource]
      ROVerification(
        IDGenerator.generate(),
        Pending,
        resource,
        RandomStringUtils.randomNumeric(10),
        RandomStringUtils.randomNumeric(10)
      )
    }

  }

  object ThankExceptionGenerator extends Generator[ThankException] {

    override def generate(): ThankException = {
      if (Random.nextBoolean())
        some[RepositoryException]
      else
        some[UserException]
    }

  }

  object ThankGenerator extends Generator[Thank] {

    override def generate(): Thank = {
      Thank(
        some[Resource],
        IDGenerator.generate(),
        nextLong(0, Long.MaxValue)
      )
    }

  }

  object ThankTransactionGenerator extends Generator[ThankTransaction] {

    override def generate(): ThankTransaction = {
      if (nextInt(0, 1) == 0)
        ThankTransaction.debit(some[User].id, some[Resource], nextLong(0, Long.MaxValue))
      else
        ThankTransaction.credit(some[User].id, some[Resource], nextLong(0, Long.MaxValue))
    }

  }

  object UserExceptionGenerator extends Generator[UserException] {

    override def generate(): UserException = UserException.notEnoughFunds()

  }

  object UserGenerator extends Generator[User] {

    override def generate(): User = {
      User(
        id = random(10),
        firstName = Some(random(10)),
        lastName = Some(random(10)),
        owns = Set.empty,
        balance = 200L,
        total = 200L,
        bankDetails = some[BankDetails],
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
