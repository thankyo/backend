package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.common.{ServiceSpec, ThankSpecification}
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{BankDetails, EOMCharge, EOMStatus}
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.junit.runner.RunWith
import scala.concurrent.duration._
import org.specs2.runner.JUnitRunner

class EOMServiceSpec extends GenericEOMServiceSpec with ServiceSpec with TestStripeUtils {

  val service = dependency[EOMService]
  val chargeRepo = dependency[EOMChargeRepository]
  val bankDetails = dependency[BankDetailsService]

  override def getStatus(yom: YearMonth) = await(service.getStatus(yom))
  override def run(yom: YearMonth) = await(service.run(yom))

  override def charges(user: UserID): Seq[EOMCharge] = chargeRepo.findByUser(user).toSeq()

  override def addBankDetails(user: UserID): BankDetails = {
    await(bankDetails.updateBankDetails(user, someValidStripeToken()))
  }

}

trait GenericEOMServiceSpec extends ThankSpecification {

  sequential

  def getStatus(yom: YearMonth): Option[EOMStatus]
  def run(yom: YearMonth): EOMStatus

  def charges(user: UserID): Seq[EOMCharge]
  def createUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): UserID
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

}