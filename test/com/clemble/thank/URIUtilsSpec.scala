package com.clemble.thank

import com.clemble.thank.util.URIUtils
import org.specs2.mutable.Specification

class URIUtilsSpec extends Specification {

  "splits correctly" in {
    val parts = URIUtils.split("http://example.com/some/what")
    parts must beEqualTo(List("http", "example.com", "some", "what"))
  }

  "to parents" in {
    val parts = URIUtils.toParents("http://example.com/some/what")
    parts must beEqualTo(List(
      "http/example.com/some/what",
      "http/example.com/some",
      "http/example.com"
    ))
  }


}
