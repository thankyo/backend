package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ThankSpecification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.{JsValue, Json}

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class WHOISServiceSpec extends ThankSpecification {

  def readJson(path: String): JsValue = {
    val fileUrl = getClass.getResource(path)
    val jsonStr = Source.fromURL(fileUrl, "UTF-8").mkString
    Json.parse(jsonStr)
  }

  import SimpleWHOISService._

  "Read whois records" should {
    whoisToEmail(readJson("./whois.clemble.json")) shouldEqual Some("bacd8ee393@clemble.com.whoistrustee.com")
    whoisToEmail(readJson("./whois.readms.json")) shouldEqual Some("readms.net-owner-c4nv@customers.whoisprivacycorp.com")
    whoisToEmail(readJson("./whois.science.json")) shouldEqual Some("editor@sciencedaily.com")
    whoisToEmail(readJson("./whois.zenpencils.json")) shouldEqual Some("ZENPENCILS.COM@domainsbyproxy.com")
  }

}
