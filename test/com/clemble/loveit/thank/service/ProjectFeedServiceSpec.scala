package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ThankSpecification
import com.clemble.loveit.thank.model.Project
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

import scala.xml.XML

@RunWith(classOf[JUnitRunner])
class ProjectFeedServiceSpec extends ThankSpecification {

  "Read RSS feed from zenpencil" in {
    val project = someRandom[Project]
    val url = getClass.getResource("zenpencil.rss.xml")
    val rssSource = XML.load(url)
    ProjectFeedService.readRSS(project, rssSource) shouldNotEqual List()
  }

  "Read RSS feed from clemble" in {
    val project = someRandom[Project]
    val url = getClass.getResource("clemble.rss.xml")
    val rssSource = XML.load(url)
    ProjectFeedService.readRSS(project, rssSource) shouldNotEqual List()
  }

  "Read RSS feed from Mangastream" in {
    val project = someRandom[Project]
    val url = getClass.getResource("mangastream.rss.xml")
    val rssSource = XML.load(url)
    ProjectFeedService.readRSS(project, rssSource) shouldNotEqual List()
  }

}
