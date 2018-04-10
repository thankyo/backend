package com.clemble.loveit.thank.model

import com.clemble.loveit.common.ThankSpecification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OpenGraphObjectSpec extends ThankSpecification {

  import com.clemble.loveit.common.model.OpenGraphObject._

  "ROOT of url extraction" in {
    getRootUrl("https://example.com/some/new") shouldEqual "https://example.com/"
    getRootUrl("https://example.com") shouldEqual "https://example.com/"

    getRootUrl("http://example.com") shouldEqual "http://example.com/"
    getRootUrl("http://example.com/some/new") shouldEqual "http://example.com/"
  }

}
