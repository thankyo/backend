package com.clemble.thank.service.repository

import com.clemble.thank.model.Thank
import com.clemble.thank.test.util.ThankGenerator
import com.clemble.thank.util.URIUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ThankRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repository = application.injector.instanceOf[ThankRepository]

  def findAll(allUrls: Seq[String]): Future[Seq[Thank]] = {
    val searchQuery: Future[Seq[Option[Thank]]] = Future.sequence(allUrls.map(uri => repository.findByResource(uri)))
    searchQuery.map(_.flatten)
  }

  "CREATE" should {

    "create all parents" in {
      val thank = ThankGenerator.generate()
      val urlParents = URIUtils.toParents(thank.resource)

      val allCreatedUri = for {
        _ <- repository.save(thank)
        search <- findAll(urlParents).map(_.map(_.resource))
      } yield {
        search
      }

      allCreatedUri must beEqualTo(urlParents).await
    }

  }

  "INCREASE" should {

    def increaseAll(urls: List[String]): Future[Boolean] = {
      Future.sequence(urls.map(repository.increase(_))).map(_.forall(_ == true))
    }

    "increase all parents" in {
      val thank = ThankGenerator.generate()
      val urlParents = URIUtils.toParents(thank.resource)

      val fAllThanks = for {
        _ <- repository.save(thank)
        _ <- increaseAll(urlParents)
        allThanks <- findAll(urlParents).map(_.map(_.given))
      } yield {
        allThanks
      }

      val expectedThanks = urlParents.reverse.zipWithIndex.map(_._2 + 1)
      fAllThanks must await(beEqualTo(expectedThanks))
    }

  }

}
