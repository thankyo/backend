package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.test.util.ThankGenerator
import com.clemble.loveit.thank.model.Thank
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ThankRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repository = dependency[ThankRepository]

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

      await(allCreatedUri) must beEqualTo(urlParents)
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
