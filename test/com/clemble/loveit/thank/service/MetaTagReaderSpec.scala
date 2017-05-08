package com.clemble.loveit.thank.service

import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MetaTagReaderSpec(implicit val ee: ExecutionEnv) extends Specification {

  "READ meta with different number of spaces" in {
    MetaTagReader.findInHtml("<meta name=\"loveit-site-verification\" content=\"1\"") shouldEqual Some("1")
    MetaTagReader.findInHtml("<meta    name=\"loveit-site-verification\"  content=\"1\"") shouldEqual Some("1")
    MetaTagReader.findInHtml("<meta    name=\"loveit-site-verification\"content=\"1\"") shouldEqual Some("1")
    MetaTagReader.findInHtml("<metaname=\"loveit-site-verification\" content=\"1\"") shouldEqual None
  }
}
