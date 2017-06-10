package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.common.{ServiceSpec, ThankSpecification}
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{EOMCharge, EOMStatus}
import com.clemble.loveit.payment.service.repository.EOMChargeRepository
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EOMServiceSpec extends GenericEOMServiceSpec with ServiceSpec {

  val service = dependency[EOMService]
  val chargeRepo = dependency[EOMChargeRepository]

  override def getStatus(yom: YearMonth) = await(service.getStatus(yom))
  override def run(yom: YearMonth) = await(service.run(yom))

  override def charges(user: UserID): Seq[EOMCharge] = chargeRepo.findByUser(user).toSeq()

}

trait GenericEOMServiceSpec extends ThankSpecification {

  def getStatus(yom: YearMonth): Option[EOMStatus]
  def run(yom: YearMonth): EOMStatus

  def charges(user: UserID): Seq[EOMCharge]
  def createUser(socialProfile: CommonSocialProfile = someRandom[CommonSocialProfile]): UserID

  // This one can finish since it runs on all of the users at the time, so transaction might take more, than 40 seconds
  "EOM run set's finished" in {
    val yom = someRandom[YearMonth]

    val status = run(yom)
    status.finished shouldEqual None

    eventually(getStatus(yom).get.finished shouldNotEqual None)
  }

  "EOM can't be run twice" in {
    val yom = someRandom[YearMonth]

    run(yom)
    run(yom) should throwA[RepositoryException]
  }

  "EOM creates charges" in {
    val yom = someRandom[YearMonth]
    val user = createUser()

    charges(user) shouldEqual Nil
    run(yom)
    eventually(getStatus(yom).get.finished shouldNotEqual None)

    val chargesAfterYom = charges(user)
    chargesAfterYom shouldNotEqual Nil
    chargesAfterYom.size shouldEqual 1
    chargesAfterYom(0).yom shouldEqual yom
  }

}