package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.RepositoryException
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EOMServiceSpec extends ServiceSpec {

  val service = dependency[EOMService]

  def getStatus(yom: YearMonth) = await(service.get(yom))
  def run(yom: YearMonth) = await(service.run(yom))

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
