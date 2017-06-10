package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.common.{ServiceSpec, ThankSpecification}
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.payment.model.{BankDetails, ChargeStatus, EOMCharge, EOMStatus, ThankTransaction}
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.duration._

class EOMServiceSpec extends GenericEOMServiceSpec with ServiceSpec with TestStripeUtils {

  val service = dependency[EOMService]
  val chargeRepo = dependency[EOMChargeRepository]
  val bankDetails = dependency[BankDetailsService]
  val thankService = dependency[ThankTransactionService]

  override def getStatus(yom: YearMonth) = {
    await(service.getStatus(yom))
  }

  override def run(yom: YearMonth) = {
    await(service.run(yom))
  }

  override def charges(user: UserID): Seq[EOMCharge] = {
    chargeRepo.findByUser(user).toSeq()
  }

  override def addBankDetails(user: UserID): BankDetails = {
    await(bankDetails.updateBankDetails(user, someValidStripeToken()))
  }

  override def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction = {
    await(thankService.create(giver, owner, resource))
  }

  override def pendingThanks(user: UserID): Seq[ThankTransaction] = {
    thankService.list(user).toSeq()
  }

}

trait GenericEOMServiceSpec extends ThankSpecification {

  sequential

  def createUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): UserID

  def thank(giver: UserID, owner: UserID, resource: Resource): ThankTransaction

  def getStatus(yom: YearMonth): Option[EOMStatus]
  def run(yom: YearMonth): EOMStatus

  def charges(user: UserID): Seq[EOMCharge]
  def pendingThanks(user: UserID): Seq[ThankTransaction]
  def addBankDetails(user: UserID): BankDetails

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

  "EOM creates charges" in {
    val yom = someRandom[YearMonth]
    val user = createUser()
    addBankDetails(user)

    charges(user) shouldEqual Nil
    run(yom)
    eventually(getStatus(yom).get.finished shouldNotEqual None)

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
    addBankDetails(giver)

    val expectedTransactions = 1 to 30 foreach(_ => thank(giver, owner, someRandom[Resource]))
    eventually(pendingThanks(giver) shouldEqual expectedTransactions)

    run(yom)
    eventually(getStatus(yom).get.finished shouldNotEqual None)

    val chargeOpt = charges(giver).find(_.yom == yom)
    chargeOpt shouldNotEqual None

    chargeOpt.get.status shouldEqual ChargeStatus.Success
    chargeOpt.get.transactions shouldEqual expectedTransactions
    // If success there should be no pending transactions left
    pendingThanks(giver) shouldEqual List.empty
  }

  "EOM charge Success on positive amount" in {
    val yom = someRandom[YearMonth]

    val owner = createUser()
    val giver = createUser()
    addBankDetails(giver)

    val expectedTransactions = 1 to 3 foreach(_ => thank(giver, owner, someRandom[Resource]))
    eventually(pendingThanks(giver) shouldEqual expectedTransactions)

    run(yom)
    eventually(getStatus(yom).get.finished shouldNotEqual None)

    val chargeOpt = charges(giver).find(_.yom == yom)
    chargeOpt shouldNotEqual None

    chargeOpt.get.status shouldEqual ChargeStatus.UnderMin
    chargeOpt.get.transactions shouldEqual expectedTransactions
    // If UnderMin there should be no change in pending transactions
    pendingThanks(giver) shouldEqual expectedTransactions
  }

}