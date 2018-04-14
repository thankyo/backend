package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ThankSpecification
import com.clemble.loveit.common.model.Project
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.xml.XML

@RunWith(classOf[JUnitRunner])
class RSSFeedReaderSpec extends ThankSpecification {

  "Read RSS feed from zenpencil" in {
    val url = getClass.getResource("zenpencil.rss.xml")
    val rssSource = XML.load(url)
    RSSFeedReader.readFeed(rssSource) shouldNotEqual List()
  }

  "Read RSS feed from clemble" in {
    val url = getClass.getResource("clemble.rss.xml")
    val rssSource = XML.load(url)
    RSSFeedReader.readFeed(rssSource) shouldNotEqual List()
  }

  "Read RSS feed from Mangastream" in {
    val url = getClass.getResource("mangastream.rss.xml")
    val rssSource = XML.load(url)
    RSSFeedReader.readFeed(rssSource) shouldNotEqual List()
  }

  "Read RSS feed from Science" in {
    val url = getClass.getResource("science.rss.xml")
    val rssSource = XML.load(url)
    val rssFeed = RSSFeedReader.readFeed(rssSource)
    rssFeed shouldNotEqual List()
    rssFeed.map(_.image).flatten shouldNotEqual List()
  }

}
