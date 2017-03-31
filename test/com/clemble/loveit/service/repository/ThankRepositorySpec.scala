package com.clemble.loveit.service.repository

import com.clemble.loveit.model.{Resource, Thank}
import com.clemble.loveit.test.util.ThankGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ThankRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repository = application.injector.instanceOf[ThankRepository]

  def findAll(resources: Seq[Resource]): Future[Seq[Thank]] = {
    val searchQuery: Future[Seq[Option[Thank]]] = Future.sequence(resources.map(uri => repository.findByResource(uri)))
    searchQuery.map(_.flatten)
  }

  "CREATE" should {

    "create all parents" in {
      val thank = ThankGenerator.generate()
      val urlParents = thank.resource.parents()

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

    def increaseAll(resources: List[Resource]): Future[Boolean] = {
      Future.sequence(resources.map(repository.increase(_))).map(_.forall(_ == true))
    }

    "increase all parents" in {
      val thank = ThankGenerator.generate()
      val urlParents = thank.resource.parents()

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
