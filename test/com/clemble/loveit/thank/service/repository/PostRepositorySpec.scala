package com.clemble.loveit.thank.service.repository

import java.time.LocalDateTime

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.model.Thank
import com.clemble.loveit.thank.service.PostService
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

import scala.concurrent.Future

@RunWith(classOf[JUnitRunner])
class PostRepositorySpec(implicit val ee: ExecutionEnv) extends RepositorySpec {

  val postRepo = dependency[PostRepository]

  def findAll(urls: Seq[Resource]): Future[Seq[Post]] = {
    val searchQuery: Future[Seq[Option[Post]]] = Future.sequence(urls.map(url => postRepo.findByResource(url)))
    searchQuery.map(_.flatten)
  }

  def createParentThank(post: Post) = {
    val parentUrl = post.url.parents.last
    val parentProject = Post.from(parentUrl, someRandom[Project].copy(url = parentUrl))
    await(postRepo.save(parentProject))
  }

  "THANKED" should {

    "be NONE for non existent" in {
      val user = someRandom[UserID]
      val url = randomResource

      await(postRepo.isSupportedBy(user, url)) shouldEqual None
    }

    "be false for not thanked" in {
      val user = createUser()

      val post = someRandom[Post]
      await(postRepo.save(post))

      await(postRepo.isSupportedBy(user, post.url)) shouldEqual Some(false)
    }

    "be true for thanked" in {
      val user = someRandom[UserID]

      val post = someRandom[Post]
      await(postRepo.save(post))

      await(postRepo.markSupported(user, post.url))
      await(postRepo.isSupportedBy(user, post.url)) shouldEqual Some(true)
    }

  }

  "INCREASE" should {

    "increase only the nodes" in {
      val post = someRandom[Post].copy(thank = Thank())
      createParentThank(post)

      await(postRepo.save(post))
      await(postRepo.markSupported("some", post.url)) shouldEqual true

      await(postRepo.findByResource(post.url)).get.thank.given shouldEqual 1
    }

    "increase only once for the user" in {
      val post = someRandom[Post].copy(thank = Thank())
      createParentThank(post)

      await(postRepo.save(post))
      await(postRepo.markSupported("some", post.url)) shouldEqual true
      await(postRepo.markSupported("some", post.url)) shouldEqual true

      await(postRepo.findByResource(post.url)).get.thank.given shouldEqual 2
    }

  }

  "UPDATE OWNER" should {

    "create if missing" in {
      val url = randomResource
      val newProject = someRandom[Project].copy(url = url)
      val oldProject = someRandom[Project].copy(url = url)
      val post = someRandom[Post].copy(url = url, project = oldProject)

      await(postRepo.save(post)) should beTrue

      await(postRepo.updateProject(newProject)) shouldEqual true
      await(postRepo.findByResource(post.url)).map(_.project) shouldEqual Some(newProject.toPointer())
    }

    "update if exists" in {
      val url = randomResource

      val A = someRandom[Project].copy(url = url)
      val post = someRandom[Post].copy(url = url, project = A)

      await(postRepo.save(post)) shouldEqual true
      await(postRepo.updateProject(A)) shouldEqual true

      val B = someRandom[Project].copy(url = url)

      await(postRepo.updateProject(B)) shouldEqual true
      await(postRepo.findByResource(post.url)).map(_.project) shouldEqual Some(B.toPointer())
    }

    "update children" in {
      val parent = randomResource
      val child = s"${parent}/${someRandom[Long]}"

      val A = someRandom[Project].copy(url = parent)
      val B = someRandom[Project].copy(url = parent)

      await(postRepo.save(Post.from(parent, A))) shouldEqual true
      await(postRepo.save(Post.from(child, A))) shouldEqual true

      await(postRepo.updateProject(B))

      await(postRepo.findByResource(parent)).get.project shouldEqual B.toPointer()
      await(postRepo.findByResource(child)).get.project shouldEqual B.toPointer()
    }

    "update children correctly" in {
      val parent = randomResource
      val difParent = s"${parent}${someRandom[Long]}"

      val A = someRandom[Project].copy(url = parent)

      await(postRepo.save(Post.from(difParent, A))) should throwA
    }

    "don't update parent" in {
      val parent = randomResource
      val child = s"${parent}/${someRandom[Long]}"

      val original = someRandom[Project].copy(url = parent)

      await(postRepo.save(Post.from(parent, original))) shouldEqual true
      await(postRepo.save(Post.from(child, original))) shouldEqual true

      val B = someRandom[Project].copy(url = child)

      await(postRepo.updateProject(B))

      await(postRepo.findByResource(parent)).get.project shouldEqual original.toPointer()
      await(postRepo.findByResource(child)).get.project shouldEqual B.toPointer()
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

  def createPostsWithProject(project: Project): Seq[Post] = {
    for {
      _ <- 1 to 10
    } yield {
      val childUrl = s"${project.url}/${someRandom[String]}"
      val post = someRandom[Post].copy(project = project, url = childUrl)
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

  "DELETE" should {

    "REMOVE all" in {
      val user = createUser()

      val prj = createProject(user)
      val posts = createPostsWithProject(prj)

      await(postRepo.deleteAll(user, prj.url)) shouldEqual true

      val postsAfterDelete = posts.map(post => await(postRepo.findByResource(post.url)))
      postsAfterDelete.forall(_.isEmpty) shouldEqual true
    }

    "IGNORE REMOVE from different user" in {
      val prj = createProject()
      val posts = createPostsWithProject(prj)

      val user = createUser()
      await(postRepo.deleteAll(user, prj.url)) shouldEqual true

      val postsAfterDelete = posts.map(post => await(postRepo.findByResource(post.url)))
      postsAfterDelete.forall(_.isDefined) shouldEqual true
    }

  }

  "Search order" should {

    "order by project & author return in publish date order" in {
      val prj = createProject()

      val posts = for {
        minusDays <- 1 to 5
      } yield {
        val childUrl = s"${prj.url}/${someRandom[String]}"
        val post = someRandom[Post]
        val modPost = post.copy(
          project = prj,
          url = childUrl,
          ogObj = post.ogObj.copy(pubDate = Some(LocalDateTime.now().minusDays(minusDays)))
        )
        await(postRepo.save(modPost)) shouldEqual true
        modPost
      }

      val authorPosts = await(postRepo.findByAuthor(prj.user))
      authorPosts.map(_.ogObj.pubDate.get).zipWithIndex shouldEqual posts.map(_.ogObj.pubDate.get).zipWithIndex

      val projectPosts = await(postRepo.findByProject(prj._id))
      projectPosts.map(_.ogObj.pubDate.get).zipWithIndex shouldEqual posts.map(_.ogObj.pubDate.get).zipWithIndex
    }

    "order by tag return in publish date order" in {
      val tag = someRandom[String]
      val posts = for {
        minusDays <- 1 to 5
      } yield {
        val prj = createProject()
        val childUrl = s"${prj.url}/${someRandom[String]}"
        val post = someRandom[Post]
        val modPost = post.copy(
          project = prj,
          url = childUrl,
          ogObj = post.ogObj.copy(
            pubDate = Some(LocalDateTime.now().minusDays(minusDays)),
            tags = Set(tag)
          )
        )
        await(postRepo.save(modPost)) shouldEqual true
        modPost
      }

      val tagPosts = await(postRepo.findByTags(Set(tag)))
      tagPosts.map(_.ogObj.pubDate.get).zipWithIndex shouldEqual posts.map(_.ogObj.pubDate.get).zipWithIndex
    }

  }

}
