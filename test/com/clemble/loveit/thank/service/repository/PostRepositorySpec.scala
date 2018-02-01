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

  val postRepo = dependency[PostRepository]

  def findAll(resources: Seq[Resource]): Future[Seq[Post]] = {
    val searchQuery: Future[Seq[Option[Post]]] = Future.sequence(resources.map(uri => postRepo.findByResource(uri)))
    searchQuery.map(_.flatten)
  }

  def createParentThank(post: Post) = {
    val parentResource = post.resource.parents.last
    val parentProject = Post.from(parentResource, someRandom[SupportedProject].copy(resource = parentResource))
    await(postRepo.save(parentProject))
  }

  "THANKED" should {

    "be NONE for non existent" in {
      val user = someRandom[UserID]
      val resource = someRandom[Resource]

      await(postRepo.isSupportedBy(user, resource)) shouldEqual None
    }

    "be false for not thanked" in {
      val user = createUser()

      val post = someRandom[Post]
      await(postRepo.save(post))

      await(postRepo.isSupportedBy(user, post.resource)) shouldEqual Some(false)
    }

    "be true for thanked" in {
      val user = someRandom[UserID]

      val post = someRandom[Post]
      await(postRepo.save(post))

      await(postRepo.markSupported(user, post.resource))
      await(postRepo.isSupportedBy(user, post.resource)) shouldEqual Some(true)
    }

  }

  "INCREASE" should {

    "increase only the nodes" in {
      val post = someRandom[Post].copy(thank = Thank())
      createParentThank(post)

      await(postRepo.save(post))
      await(postRepo.markSupported("some", post.resource)) shouldEqual true

      await(postRepo.findByResource(post.resource)).get.thank.given shouldEqual 1
    }

    "increase only once for the user" in {
      val post = someRandom[Post].copy(thank = Thank())
      createParentThank(post)

      await(postRepo.save(post))
      await(postRepo.markSupported("some", post.resource)) shouldEqual true
      await(postRepo.markSupported("some", post.resource)) shouldEqual false

      await(postRepo.findByResource(post.resource)).get.thank.given shouldEqual 1
    }

  }

  "UPDATE OWNER" should {

    "create if missing" in {
      val resource = someRandom[Resource]
      val newProject = someRandom[SupportedProject].copy(resource = resource)
      val oldProject = someRandom[SupportedProject].copy(resource = resource)
      val post = someRandom[Post].copy(resource = resource, project = oldProject)

      await(postRepo.save(post)) should beTrue

      await(postRepo.updateProject(newProject)) shouldEqual true
      await(postRepo.findByResource(post.resource)).map(_.project) shouldEqual Some(newProject)
    }

    "update if exists" in {
      val resource = someRandom[Resource]

      val A = someRandom[SupportedProject].copy(resource = resource)
      val post = someRandom[Post].copy(resource = resource, project = A)

      await(postRepo.save(post)) shouldEqual true
      await(postRepo.updateProject(A)) shouldEqual true

      val B = someRandom[SupportedProject].copy(resource = resource)

      await(postRepo.updateProject(B)) shouldEqual true
      await(postRepo.findByResource(post.resource)).map(_.project) shouldEqual Some(B)
    }

    "update children" in {
      val parent = someRandom[Resource]
      val child = HttpResource(s"${parent.uri}/${someRandom[Long]}")

      val A = someRandom[SupportedProject].copy(resource = parent)
      val B = someRandom[SupportedProject].copy(resource = parent)

      await(postRepo.save(Post.from(parent, A))) shouldEqual true
      await(postRepo.save(Post.from(child, A))) shouldEqual true

      await(postRepo.updateProject(B))

      await(postRepo.findByResource(parent)).get.project shouldEqual B
      await(postRepo.findByResource(child)).get.project shouldEqual B
    }

    "update children correctly" in {
      val parent = someRandom[HttpResource]
      val difParent = HttpResource(s"${parent.uri}${someRandom[Long]}")

      val A = someRandom[SupportedProject].copy(resource = parent)

      await(postRepo.save(Post.from(difParent, A))) should throwA
    }

    "don't update parent" in {
      val parent = someRandom[HttpResource]
      val child = HttpResource(s"${parent.uri}/${someRandom[Long]}")

      val original = someRandom[SupportedProject].copy(resource = parent)

      await(postRepo.save(Post.from(parent, original))) shouldEqual true
      await(postRepo.save(Post.from(child, original))) shouldEqual true

      val B = someRandom[SupportedProject].copy(resource = child)

      await(postRepo.updateProject(B))

      await(postRepo.findByResource(parent)).get.project shouldEqual original
      await(postRepo.findByResource(child)).get.project shouldEqual B
    }

  }

  "TAG Search" should {

    def createPostsWithTag(tag: String): Seq[Post] = {
      for {
        _ <- 1 to 10
      } yield {
        val post = someRandom[Post]
        val postWithTag = post.copy(ogObj = post.ogObj.copy(tags = Set(tag)))
        await(postRepo.save(postWithTag)) shouldEqual true
        postWithTag
      }
    }

    "simple search" in {
      val tag = someRandom[String]
      val posts = createPostsWithTag(tag)

      await(postRepo.findByTags(Set(tag))) should containAllOf(posts)
    }

    "search with multiple tags" in {
      val tagA = someRandom[String]
      val tagB = someRandom[String]

      val postsWithA = createPostsWithTag(tagA)
      val postsWithB = createPostsWithTag(tagB)

      await(postRepo.findByTags(Set(tagA, tagB))) should containAllOf(postsWithA ++ postsWithB)
    }

  }

  def createPostsWithProject(project: SupportedProject): Seq[Post] = {
    for {
      _ <- 1 to 10
    } yield {
      val childResource = Resource.from(s"${project.resource.stringify()}/${someRandom[String]}")
      val post = someRandom[Post].copy(project = project, resource = childResource)
      await(postRepo.save(post)) shouldEqual true
      post
    }
  }

  "AUTHOR search" should {
    val user = createUser()

    val postsProjectA = createPostsWithProject(createProject(user))
    val postsProjectB = createPostsWithProject(createProject(user))

    await(postRepo.findByAuthor(user)) should containAllOf(postsProjectA ++ postsProjectB)
  }

  "PROJECT search" should {
    val user = createUser()

    val projectA = createProject(user)
    val postsProjectA = createPostsWithProject(projectA)

    val projectB = createProject(user)
    val postsProjectB = createPostsWithProject(projectB)

    await(postRepo.findByProject(projectA._id)) should containAllOf(postsProjectA)
    await(postRepo.findByProject(projectB._id)) should containAllOf(postsProjectB)
  }

}
