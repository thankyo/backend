package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.test.util.ThankGenerator
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.ThankService
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ThankRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repository = dependency[ThankRepository]
  val service = dependency[ThankService]

  def findAll(resources: Seq[Resource]): Future[Seq[Thank]] = {
    val searchQuery: Future[Seq[Option[Thank]]] = Future.sequence(resources.map(uri => repository.findByResource(uri)))
    searchQuery.map(_.flatten)
  }

  def createOwner(thank: Thank) = {
    val ownerResource = Thank(thank.resource.parents.last, IDGenerator.generate())
    await(repository.save(ownerResource))
  }

  "CREATE" should {

    "create all parents" in {
      val thank = ThankGenerator.generate()
      createOwner(thank)

      val urlParents = thank.resource.parents()
      val allCreatedUri = for {
        _ <- service.getOrCreate(thank.resource)
        search <- findAll(urlParents).map(_.map(_.resource))
      } yield {
        search
      }

      await(allCreatedUri) must beEqualTo(urlParents)
    }

  }

  "INCREASE" should {

    def increaseAll(resources: List[Resource]): Future[Boolean] = {
      Future.sequence(resources.map(repository.increase("some", _))).map(_.forall(_ == true))
    }

    "increase only the nodes" in {
      val thank = ThankGenerator.generate().copy(given = 0)
      createOwner(thank)

      await(repository.save(thank))
      await(repository.increase("some", thank.resource)) shouldEqual true

      await(repository.findByResource(thank.resource)).get.given shouldEqual 1
    }

    "increase only once for the user" in {
      val thank = ThankGenerator.generate().copy(given = 0)
      createOwner(thank)

      await(repository.save(thank))
      await(repository.increase("some", thank.resource)) shouldEqual true
      await(repository.increase("some", thank.resource)) shouldEqual false

      await(repository.findByResource(thank.resource)).get.given shouldEqual 1
    }

  }

}
