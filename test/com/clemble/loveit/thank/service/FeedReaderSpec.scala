package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ThankSpecification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class FeedReaderSpec extends ThankSpecification{

  "parse date" should {
    import RSSParser._
    RSSParser.parseDate("Tue, 16 Jan 2018 00:03:11 +0000") shouldNotEqual None
  }

}
