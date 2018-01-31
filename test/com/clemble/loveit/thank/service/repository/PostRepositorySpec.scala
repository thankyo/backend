package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{HttpResource, Resource, UserID}
import com.clemble.loveit.thank.model.{Post, SupportedProject, Thank}
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
    val ownerResource = Post.from(res, someRandom[SupportedProject])
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
      val post = someRandom[Post]

      await(repo.save(post)) should beTrue

      await(repo.updateOwner(owner, post.resource)) shouldEqual true
      await(repo.findByResource(post.resource)).map(_.project) shouldEqual Some(owner)
    }

    "update if exists" in {
      val post = someRandom[Post]

      val A = someRandom[SupportedProject]

      await(repo.save(post)) shouldEqual true
      await(repo.updateOwner(A, post.resource)) shouldEqual true

      val B = someRandom[SupportedProject]

      await(repo.updateOwner(B, post.resource)) shouldEqual true
      await(repo.findByResource(post.resource)).map(_.project) shouldEqual Some(B)
    }

    "update children" in {
      val A = someRandom[SupportedProject]
      val B = someRandom[SupportedProject]

      val parent = someRandom[HttpResource]
      val child = HttpResource(s"${parent.uri}/${someRandom[Long]}")

      await(repo.save(Post.from(parent, A))) shouldEqual true
      await(repo.save(Post.from(child, A))) shouldEqual true

      await(repo.updateOwner(B, parent))

      await(repo.findByResource(parent)).get.project shouldEqual B
      await(repo.findByResource(child)).get.project shouldEqual B
    }

    "update children correctly" in {
      val A = someRandom[SupportedProject]
      val B = someRandom[SupportedProject]

      val parent = someRandom[HttpResource]
      val difParent = HttpResource(s"${parent.uri}${someRandom[Long]}")

      await(repo.save(Post.from(parent, A))) shouldEqual true
      await(repo.save(Post.from(difParent, A))) shouldEqual true

      await(repo.updateOwner(B, parent))

      await(repo.findByResource(parent)).get.project shouldEqual B
      await(repo.findByResource(difParent)).get.project shouldEqual A
    }

    "don't update parent" in {
      val original = someRandom[SupportedProject]
      val B = someRandom[SupportedProject]

      val parent = someRandom[HttpResource]
      val child = HttpResource(s"${parent.uri}/${someRandom[Long]}")

      await(repo.save(Post.from(parent, original))) shouldEqual true
      await(repo.save(Post.from(child, original))) shouldEqual true

      await(repo.updateOwner(B, child))

      await(repo.findByResource(parent)).get.project shouldEqual original
      await(repo.findByResource(child)).get.project shouldEqual B
    }

  }

  "TAG Search" should {

    def createPostsWithTag(tag: String): Seq[Post] = {
      for {
        _ <- 1 to 10
      } yield {
        val post = someRandom[Post]
        val postWithTag = post.copy(ogObj = post.ogObj.copy(tags = Set(tag)))
        await(repo.save(postWithTag)) shouldEqual true
        postWithTag
      }
    }

    "simple search" in {
      val tag = someRandom[String]
      val posts = createPostsWithTag(tag)

      await(repo.findByTags(Set(tag))) should containAllOf(posts)
    }

    "search with multiple tags" in {
      val tagA = someRandom[String]
      val tagB = someRandom[String]

      val postsWithA = createPostsWithTag(tagA)
      val postsWithB = createPostsWithTag(tagB)

      await(repo.findByTags(Set(tagA, tagB))) should containAllOf(postsWithA ++ postsWithB)
    }

  }

  "AUTHOR search" should {

    def createPostsWithAuthor(author: UserID): Seq[Post] = {
      val supportedProject = someRandom[SupportedProject].copy(id = author)
      for {
        _ <- 1 to 10
      } yield {
        val post = someRandom[Post].copy(project = supportedProject)
        await(repo.save(post)) shouldEqual true
        post
      }
    }

    "simple search" in {
      val author = createUser()
      val posts = createPostsWithAuthor(author)

      await(repo.findByAuthor(author)) should containAllOf(posts)
    }

  }

}
