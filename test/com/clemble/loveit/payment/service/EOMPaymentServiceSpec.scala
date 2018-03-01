package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.common.{FunctionalThankSpecification, ServiceSpec}
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.{Money, Resource}
import com.clemble.loveit.common.util.LoveItCurrency
import com.clemble.loveit.payment.PaymentTestExecutor
import com.clemble.loveit.payment.model._
import play.api.libs.json.JsUndefined

import scala.concurrent.duration._

class EOMPaymentServiceSpec extends GenericEOMPaymentServiceSpec with PaymentServiceTestExecutor {

  private val service = dependency[EOMPaymentService]

  override def getStatus(yom: YearMonth): Option[EOMStatus] = {
    await(service.getStatus(yom))
  }

  override def run(yom: YearMonth): EOMStatus = {
    await(service.run(yom))
  }

}

trait GenericEOMPaymentServiceSpec extends FunctionalThankSpecification with PaymentTestExecutor {

  sequential

  def getStatus(yom: YearMonth): Option[EOMStatus]
  def run(yom: YearMonth): EOMStatus
  def runAndWait(yom: YearMonth): EOMStatus = {
    val status = run(yom)
    status.yom shouldEqual yom
    eventually(getStatus(yom).flatMap(_.createCharges) shouldNotEqual None)
    eventually(getStatus(yom).flatMap(_.applyCharges) shouldNotEqual None)
    eventually(getStatus(yom).flatMap(_.createPayout) shouldNotEqual None)
    eventually(getStatus(yom).flatMap(_.applyPayout) shouldNotEqual None)
    eventually(getStatus(yom).flatMap(_.finished) shouldNotEqual None)
    getStatus(yom).get
  }

  "GENERAL" should {

    // This one can finish since it runs on all of the users at the time, so transaction might take more, than 40 seconds
    "EOM run set's finished" in {
      val yom = someRandom[YearMonth]

      runAndWait(yom)

      eventually(getStatus(yom).flatMap(_.finished) shouldNotEqual None)
    }

    "EOM can't be run twice" in {
      val yom = someRandom[YearMonth]

      runAndWait(yom)
      runAndWait(yom) should throwA[RepositoryException]
    }
  }

  "CHARGES NO CHARGE ACCOUNT" should {

    "EOM creates charges" in {
      val yom = someRandom[YearMonth]
      val user = createUser()
      val owner = createProject()

      1 to 50 map (_ => thank(user, owner, randomResource))

      runAndWait(yom)

      eventually(charges(user) shouldNotEqual Nil)
      val chargesAfterYom = charges(user)
      chargesAfterYom.size should beGreaterThan(0)
      chargesAfterYom.map(_.yom) should contain(yom)
      chargesAfterYom.head.amount shouldEqual Money(0, LoveItCurrency.DEFAULT)
      chargesAfterYom.head.status shouldEqual ChargeStatus.NoBankDetails
    }

  }

  "CHARGES WITH CHARGE ACCOUNT" should {

    "EOM creates charges" in {
      val yom = someRandom[YearMonth]
      val project = createProject()
      val user = createUser()
      addChargeAccount(user)

      charges(user) shouldEqual Nil

      1 to 50 map (_ => thank(user, project, randomResource))
      runAndWait(yom)

      val statusAfter = getStatus(yom)
      statusAfter.get.createCharges.get.success should beGreaterThan(0L)
      statusAfter.get.createCharges.get.total should beGreaterThan(0L)

      eventually(charges(user) shouldNotEqual Nil)
      val chargesAfterYom = charges(user)
      chargesAfterYom.size should beGreaterThan(0)
      chargesAfterYom.map(_.yom) should contain(yom)
    }

    "EOM charge Success on positive amount" in {
      val yom = someRandom[YearMonth]

      val owner = createProject()
      val giver = createUser()
      addChargeAccount(giver)

      val expectedTransactions = 1 to 50 map (_ => thank(giver, owner, randomResource))
      eventually(pendingCharges(giver) should containAllOf(expectedTransactions))

      runAndWait(yom)

      val chargeOpt = charges(giver).find(_.yom == yom)
      chargeOpt shouldNotEqual None

      val yomCharge = chargeOpt.get

      if (yomCharge.status == ChargeStatus.Failed) {
        yomCharge.details.get \ "error" shouldEqual JsUndefined
      }
      yomCharge.status shouldEqual ChargeStatus.Success
      yomCharge.transactions shouldEqual expectedTransactions
      // If success there should be no pending transactions left
      eventually(pendingCharges(giver) shouldEqual List.empty)
    }

    "EOM charge UnderMin on small thank amount" in {
      val yom = someRandom[YearMonth]

      val owner = createProject()
      val giver = createUser()
      addChargeAccount(giver)

      val expectedTransactions = 1 to 3 map (_ => thank(giver, owner, randomResource))
      pendingCharges(giver) should containAllOf(expectedTransactions)

      runAndWait(yom)

      val chargeOpt = charges(giver).find(_.yom == yom)
      chargeOpt shouldNotEqual None

      chargeOpt.get.status shouldEqual ChargeStatus.UnderMin
      chargeOpt.get.transactions shouldEqual expectedTransactions
      // If UnderMin there should be no change in pending transactions
      pendingCharges(giver) shouldEqual expectedTransactions
    }
  }

  "PAYOUT WITH BANK ACCOUNT" should {

    "EOM should generate Payout" in {
      val yom = someRandom[YearMonth]

      val owner = createProject()

      val giverA = createUser()
      addChargeAccount(giverA)

      val giverB = createUser()
      addChargeAccount(giverB)

      1 to 50 map (_ => thank(giverA, owner, randomResource))
      1 to 50 map (_ => thank(giverA, owner, randomResource))

      val finalStatus = runAndWait(yom)

      finalStatus.createPayout.get.success shouldEqual 1
      val ownerPayouts = payouts(owner.user)
      ownerPayouts.size shouldEqual 1
      ownerPayouts.head.amount shouldEqual new Money(9.0, "USD")
    }

  }

}