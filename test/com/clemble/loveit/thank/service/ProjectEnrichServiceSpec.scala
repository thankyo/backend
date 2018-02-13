package com.clemble.loveit.thank.service

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

import scala.io.Source

@RunWith(classOf[JUnitRunner])
class ProjectEnrichServiceSpec extends Specification {

  "Read tags" should {
    val url = getClass.getResource("./zenpencil.htm")
    val htmlSource: String = Source.fromURL(url, "UTF-8").mkString
    ProjectEnrichService.readTags(htmlSource) shouldEqual Set()
  }

  "Read description" should {
    val url = getClass.getResource("./zenpencil.htm")
    val htmlSource: String = Source.fromURL(url, "UTF-8").mkString
    ProjectEnrichService.readDescription(htmlSource) shouldEqual Some("")
  }
}
