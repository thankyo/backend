package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ThankSpecification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class ProjectEnrichServiceSpec extends ThankSpecification {

  "Read tags" should {
    val url = getClass.getResource("./zenpencil.htm")
    val htmlSource: String = Source.fromURL(url, "UTF-8").mkString
    ProjectEnrichService.readTags(htmlSource) shouldEqual Set("art", "quotes", "inspirational", "comics", "inspire", "motivational", "poetry", "inspiring", "cartoons", "webcomic")
  }

  "Read description" should {
    val url = getClass.getResource("./zenpencil.htm")
    val htmlSource: String = Source.fromURL(url, "UTF-8").mkString
    ProjectEnrichService.readDescription(htmlSource) shouldEqual Some("Cartoon quotes from inspirational folks")
  }
}
