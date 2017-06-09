package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.HttpResource
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

class MetaTagReaderSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  lazy val tagReader = dependency[MetaTagReader]

  "READ meta with different number of spaces" in {
    MetaTagReader.findInHtml("<meta name=\"loveit-site-verification\" content=\"1\"") shouldEqual Some("1")
    MetaTagReader.findInHtml("<meta    name=\"loveit-site-verification\"  content=\"1\"") shouldEqual Some("1")
    MetaTagReader.findInHtml("<meta    name=\"loveit-site-verification\"content=\"1\"") shouldEqual Some("1")
    MetaTagReader.findInHtml("<metaname=\"loveit-site-verification\" content=\"1\"") shouldEqual None
  }

  "IGNORE broken url" in {
    val brokenRes = HttpResource(s"http://${RandomStringUtils.randomAlphabetic(10)}.com/")
    await(tagReader.read(brokenRes)) shouldEqual None
  }

}
