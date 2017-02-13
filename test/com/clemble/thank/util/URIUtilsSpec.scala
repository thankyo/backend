package com.clemble.thank.util

import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class URIUtilsSpec extends Specification {

  "generate all parent URL's" in {
    val parts = URIUtils.toParents("http/example.com/some/what")
    parts must beEqualTo(List(
      "example.com/some/what",
      "example.com/some",
      "example.com"
    ))
  }

  "normalize" in {
    val uri = "example.com/some/what"
    for {
      variation <- URIUtilsSpec.generateVariations(uri)
    } yield {
      URIUtils.normalize(variation) shouldEqual uri
    }
  }

}

object URIUtilsSpec {

  def generateVariations(masterURI: String): List[String] = {
    List(
      masterURI,
      s"http//$masterURI",
      s"https//$masterURI",
      s"http/////$masterURI",
      s"https/////$masterURI",
      s"http/$masterURI",
      s"https/$masterURI"
    )
  }

}