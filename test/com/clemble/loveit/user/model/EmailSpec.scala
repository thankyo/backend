package com.clemble.loveit.user.model

import com.clemble.loveit.common.model._
import org.junit.runner.RunWith
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EmailSpec extends Specification {

  "Email domain " should {
    "some@example.com".toEmailDomain() shouldEqual "example.com"
    "somewhat@exa-mple.net".toEmailDomain() shouldEqual "exa-mple.net"
  }

}
