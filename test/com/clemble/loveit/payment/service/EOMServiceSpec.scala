package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.common.{ServiceSpec, ThankSpecification}
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.payment.model.EOMStatus
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EOMServiceSpec extends GenericEOMServiceSpec with ServiceSpec {

  val service = dependency[EOMService]

  override def getStatus(yom: YearMonth) = await(service.getStatus(yom))
  override def run(yom: YearMonth) = await(service.run(yom))

}

trait GenericEOMServiceSpec extends ThankSpecification {

  def getStatus(yom: YearMonth): Option[EOMStatus]
  def run(yom: YearMonth): EOMStatus

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

}