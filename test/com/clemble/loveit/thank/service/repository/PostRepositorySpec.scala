package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, SupportedProject, Thank}
import com.clemble.loveit.thank.service.PostService
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class PostRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val repo = dependency[PostRepository]
  val service = dependency[PostService]

  def findAll(resources: Seq[Resource]): Future[Seq[Post]] = {
    val searchQuery: Future[Seq[Option[Post]]] = Future.sequence(resources.map(uri => repo.findByResource(uri)))
    searchQuery.map(_.flatten)
  }

  def createParentThank(post: Post) = {
    val res = post.resource.parents.last
    val ownerResource = Post(res, someRandom[SupportedProject], OpenGraphObject(res.stringify()))
    await(repo.save(ownerResource))
  }

  "THANKED" should {

    "be NONE for non existent" in {
      val user = someRandom[UserID]
      val resource = someRandom[Resource]

      await(repo.isSupportedBy(user, resource)) shouldEqual None
    }

    "be false for not thanked" in {
      val user = someRandom[UserID]

      val post = someRandom[Post]
      await(repo.save(post))

      await(repo.isSupportedBy(user, post.resource)) shouldEqual Some(false)
    }

    "be true for thanked" in {
      val user = someRandom[UserID]

      val post = someRandom[Post]
      await(repo.save(post))

      await(repo.markSupported(user, post.resource))
      await(repo.isSupportedBy(user, post.resource)) shouldEqual Some(true)
    }

  }

  "CREATE" should {

    "create all parents" in {
      val post = someRandom[Post]
      createParentThank(post)

      val urlParents = post.resource.parents()
      val allCreatedUri = for {
        _ <- service.getOrCreate(post.resource)
        search <- findAll(urlParents).map(_.map(_.resource))
      } yield {
        search
      }

      await(allCreatedUri) must beEqualTo(urlParents)
    }

  }

  "INCREASE" should {

    "increase only the nodes" in {
      val post = someRandom[Post].copy(thank = Thank())
      createParentThank(post)

      await(repo.save(post))
      await(repo.markSupported("some", post.resource)) shouldEqual true

      await(repo.findByResource(post.resource)).get.thank.given shouldEqual 1
    }

    "increase only once for the user" in {
      val post = someRandom[Post].copy(thank = Thank())
      createParentThank(post)

      await(repo.save(post))
      await(repo.markSupported("some", post.resource)) shouldEqual true
      await(repo.markSupported("some", post.resource)) shouldEqual false

      await(repo.findByResource(post.resource)).get.thank.given shouldEqual 1
    }

  }

  "UPDATE OWNER" should {

    "create if missing" in {
      val owner = someRandom[SupportedProject]
      val resource = someRandom[Resource]

      await(repo.findByResource(resource)) shouldEqual None

      await(repo.updateOwner(owner, resource)) shouldEqual true
      await(repo.findByResource(resource)) shouldNotEqual None
    }

    "update if exists" in {
      val resource = someRandom[Resource]

      val A = someRandom[SupportedProject]

      await(repo.updateOwner(A, resource)) shouldEqual true
      await(repo.findByResource(resource)).get.project shouldEqual A

      val B = someRandom[SupportedProject]

      await(repo.updateOwner(B, resource)) shouldEqual true
      await(repo.findByResource(resource)).get.project shouldEqual B
    }

    "update children" in {
      val A = someRandom[SupportedProject]
      val B = someRandom[SupportedProject]

      val parent = someRandom[HttpResource]
      val child = HttpResource(s"${parent.uri}/${someRandom[Long]}")

      await(repo.save(Post(parent, A, OpenGraphObject(parent.stringify())))) shouldEqual true
      await(repo.save(Post(child, A, OpenGraphObject(child.stringify())))) shouldEqual true

      await(repo.updateOwner(B, parent))

      await(repo.findByResource(parent)).get.project shouldEqual B
      await(repo.findByResource(child)).get.project shouldEqual B
    }

    "update children correctly" in {
      val A = someRandom[SupportedProject]
      val B = someRandom[SupportedProject]

      val parent = someRandom[HttpResource]
      val difParent = HttpResource(s"${parent.uri}${someRandom[Long]}")

      await(repo.save(Post(parent, A, OpenGraphObject(parent.stringify())))) shouldEqual true
      await(repo.save(Post(difParent, A, OpenGraphObject(difParent.stringify())))) shouldEqual true

      await(repo.updateOwner(B, parent))

      await(repo.findByResource(parent)).get.project shouldEqual B
      await(repo.findByResource(difParent)).get.project shouldEqual A
    }

    "don't update parent" in {
      val original = someRandom[SupportedProject]
      val B = someRandom[SupportedProject]

      val parent = someRandom[HttpResource]
      val child = HttpResource(s"${parent.uri}/${someRandom[Long]}")

      await(repo.save(Post(parent, original, OpenGraphObject(parent.stringify())))) shouldEqual true
      await(repo.save(Post(child, original, OpenGraphObject(child.stringify())))) shouldEqual true

      await(repo.updateOwner(B, child))

      await(repo.findByResource(parent)).get.project shouldEqual original
      await(repo.findByResource(child)).get.project shouldEqual B
    }

  }

}
