package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{HttpResource, Resource}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.Thank
import com.clemble.loveit.thank.service.ThankService
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner
import org.apache.commons.lang3.RandomStringUtils._

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class ThankRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[ThankRepository]
  val service = dependency[ThankService]

  def findAll(resources: Seq[Resource]): Future[Seq[Thank]] = {
    val searchQuery: Future[Seq[Option[Thank]]] = Future.sequence(resources.map(uri => repo.findByResource(uri)))
    searchQuery.map(_.flatten)
  }

  def createParentThank(thank: Thank) = {
    val ownerResource = Thank(thank.resource.parents.last, IDGenerator.generate())
    await(repo.save(ownerResource))
  }

  "DEFAULT exists" in {
    eventually(await(repo.findByResource(Thank.INTEGRATION_DEFAULT.resource)) shouldNotEqual None)
  }

  "THANKED" should {

    "be NONE for non existent" in {
      val user = IDGenerator.generate()
      val resource = someRandom[Resource]

      await(repo.thanked(user, resource)) shouldEqual None
    }

    "be false for not thanked" in {
      val user = IDGenerator.generate()

      val thank = someRandom[Thank]
      await(repo.save(thank))

      await(repo.thanked(user, thank.resource)) shouldEqual Some(false)
    }

    "be true for thanked" in {
      val user = IDGenerator.generate()

      val thank = someRandom[Thank]
      await(repo.save(thank))

      await(repo.increase(user, thank.resource))
      await(repo.thanked(user, thank.resource)) shouldEqual Some(true)
    }

  }

  "CREATE" should {

    "create all parents" in {
      val thank = someRandom[Thank]
      createParentThank(thank)

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

    "increase only the nodes" in {
      val thank = someRandom[Thank].copy(given = 0)
      createParentThank(thank)

      await(repo.save(thank))
      await(repo.increase("some", thank.resource)) shouldEqual true

      await(repo.findByResource(thank.resource)).get.given shouldEqual 1
    }

    "increase only once for the user" in {
      val thank = someRandom[Thank].copy(given = 0)
      createParentThank(thank)

      await(repo.save(thank))
      await(repo.increase("some", thank.resource)) shouldEqual true
      await(repo.increase("some", thank.resource)) shouldEqual false

      await(repo.findByResource(thank.resource)).get.given shouldEqual 1
    }

  }

  "UPDATE OWNER" should {

    "create if missing" in {
      val owner = IDGenerator.generate()
      val resource = someRandom[Resource]

      await(repo.findByResource(resource)) shouldEqual None

      await(repo.updateOwner(owner, resource)) shouldEqual true
      await(repo.findByResource(resource)) shouldNotEqual None
    }

    "update if exists" in {
      val resource = someRandom[Resource]

      val A = IDGenerator.generate()

      await(repo.updateOwner(A, resource)) shouldEqual true
      await(repo.findByResource(resource)).get.owner shouldEqual A

      val B = IDGenerator.generate()

      await(repo.updateOwner(B, resource)) shouldEqual true
      await(repo.findByResource(resource)).get.owner shouldEqual B
    }

    "update children" in {
      val A = IDGenerator.generate()
      val B = IDGenerator.generate()

      val parentUri = s"${randomNumeric(10)}.com/${randomNumeric(2)}"
      val parent = HttpResource(parentUri)
      val child = HttpResource(s"${parentUri}/${randomNumeric(3)}")

      await(repo.save(Thank(parent, A))) shouldEqual true
      await(repo.save(Thank(child, A))) shouldEqual true

      await(repo.updateOwner(B, parent))

      await(repo.findByResource(parent)).get.owner shouldEqual B
      await(repo.findByResource(child)).get.owner shouldEqual B
    }

    "update children correctly" in {
      val A = IDGenerator.generate()
      val B = IDGenerator.generate()

      val parentUri = s"${randomNumeric(10)}.com/${randomNumeric(2)}"
      val parent = HttpResource(parentUri)
      val difParent = HttpResource(s"${parentUri}${randomNumeric(3)}")

      await(repo.save(Thank(parent, A))) shouldEqual true
      await(repo.save(Thank(difParent, A))) shouldEqual true

      await(repo.updateOwner(B, parent))

      await(repo.findByResource(parent)).get.owner shouldEqual B
      await(repo.findByResource(difParent)).get.owner shouldEqual A
    }

    "don't update parent" in {
      val original = IDGenerator.generate()
      val B = IDGenerator.generate()

      val parentUri = s"${randomNumeric(10)}.com/${randomNumeric(2)}"
      val parent = HttpResource(parentUri)
      val child = HttpResource(s"${parentUri}/${randomNumeric(3)}")

      await(repo.save(Thank(parent, original))) shouldEqual true
      await(repo.save(Thank(child, original))) shouldEqual true

      await(repo.updateOwner(B, child))

      await(repo.findByResource(parent)).get.owner shouldEqual original
      await(repo.findByResource(child)).get.owner shouldEqual B
    }

  }

}
