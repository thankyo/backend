package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.auth.models.requests.SignUpRequest
import com.clemble.loveit.common.{FunctionalThankSpecification, ServiceSpec, ThankSpecification}
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model._
import com.clemble.loveit.payment.service.repository.{EOMChargeRepository, EOMPayoutRepository}
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.duration._

class EOMServiceSpec extends GenericEOMServiceSpec with PaymentServiceTestExecutor {

  val service = dependency[EOMService]

  override def getStatus(yom: YearMonth) = {
    await(service.getStatus(yom))
  }

  override def run(yom: YearMonth) = {
    await(service.run(yom))
  }

}

trait GenericEOMServiceSpec extends FunctionalThankSpecification with PaymentTestExecutor {

  sequential

  def getStatus(yom: YearMonth): Option[EOMStatus]
  def run(yom: YearMonth): EOMStatus
  def runAndWait(yom: YearMonth): EOMStatus = {
    run(yom)
    eventually(getStatus(yom).get.finished shouldNotEqual None)
    getStatus(yom).get
  }

  "GENERAL" should {

    // This one can finish since it runs on all of the users at the time, so transaction might take more, than 40 seconds
    "EOM run set's finished" in {
      val yom = someRandom[YearMonth]

      val status = run(yom)
      status.finished shouldEqual None

      eventually(40, 1 second)(getStatus(yom).get.finished shouldNotEqual None)
    }

    "EOM can't be run twice" in {
      val yom = someRandom[YearMonth]

      run(yom)
      run(yom) should throwA[RepositoryException]
    }
  }

  "CHARGES" should {

    "EOM creates charges" in {
      val yom = someRandom[YearMonth]
      val user = createUser()
      addChargeAccount(user)

      charges(user) shouldEqual Nil

      runAndWait(yom)

      val statusAfter = getStatus(yom)
      statusAfter.get.createCharges.success should beGreaterThan(0L)
      statusAfter.get.createCharges.total should beGreaterThan(0L)

      eventually(40, 1 second)(charges(user) shouldNotEqual Nil)
      val chargesAfterYom = charges(user)
      chargesAfterYom.size should beGreaterThan(0)
      chargesAfterYom.map(_.yom) should contain(yom)
    }


    "EOM charge Success on positive amount" in {
      val yom = someRandom[YearMonth]

      val owner = createUser()
      val giver = createUser()
      addChargeAccount(giver)

      val expectedTransactions = 1 to 30 map (_ => thank(giver, owner, someRandom[Resource]))
      pendingThanks(giver) should containAllOf(expectedTransactions)

      runAndWait(yom)

      val chargeOpt = charges(giver).find(_.yom == yom)
      chargeOpt shouldNotEqual None

      chargeOpt.get.status shouldEqual ChargeStatus.Success
      chargeOpt.get.transactions shouldEqual expectedTransactions
      // If success there should be no pending transactions left
      pendingThanks(giver) shouldEqual List.empty
    }

    "EOM charge UnderMin on small thank amount" in {
      val yom = someRandom[YearMonth]

      val owner = createUser()
      val giver = createUser()
      addChargeAccount(giver)

      val expectedTransactions = 1 to 3 map (_ => thank(giver, owner, someRandom[Resource]))
      pendingThanks(giver) should containAllOf(expectedTransactions)

      runAndWait(yom)

      val chargeOpt = charges(giver).find(_.yom == yom)
      chargeOpt shouldNotEqual None

      chargeOpt.get.status shouldEqual ChargeStatus.UnderMin
      chargeOpt.get.transactions shouldEqual expectedTransactions
      // If UnderMin there should be no change in pending transactions
      pendingThanks(giver) shouldEqual expectedTransactions
    }
  }

  "PAYOUT" should {

    "EOM should generate Payout" in {
      val yom = someRandom[YearMonth]

      val owner = createUser()

      val giverA = createUser()
      addChargeAccount(giverA)

      val giverB = createUser()
      addChargeAccount(giverB)

      1 to 30 map (_ => thank(giverA, owner, someRandom[Resource]));
      1 to 30 map (_ => thank(giverA, owner, someRandom[Resource]));

      val finalStatus = runAndWait(yom)

      finalStatus.createPayout.success shouldEqual 1
      val ownerPayouts = payouts(owner)
      ownerPayouts.size shouldEqual 1
      ownerPayouts(0).amount shouldEqual new Money(5.4, "USD")
    }

  }

}