package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.error.{PaymentException, ResourceException}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service._
import com.clemble.loveit.common.model.Project
import com.clemble.loveit.thank.service.repository.PostRepository

import scala.concurrent.{ExecutionContext, Future}

trait PostService {

  def findById(id: PostID): Future[Option[Post]]

  def findByTags(tags: Set[Tag]): Future[List[Post]]

  def findByAuthor(author: UserID): Future[List[Post]]

  def findByProject(project: ProjectID): Future[List[Post]]

  def findLastByProject(project: ProjectID): Future[Option[Post]]

  def getPostOrProject(url: Resource): Future[Either[Post, Project]]

  def create(og: OpenGraphObject): Future[Post]

  def refresh(user: UserID, post: PostID): Future[Post]

  def updateProject(owner: Project): Future[Boolean]

  def delete(id: PostID): Future[Boolean]

  def delete(project: Project): Future[Boolean]

  def hasSupported(giver: UserID, url: Resource): Future[Boolean]

  def thank(supporter: UserID, url: Resource): Future[Post]

}

@Singleton
case class SimplePostService @Inject()(
  postEventBus: PostEventBus,
  thankEventBus: ThankEventBus,
  lookupService: ProjectLookupService,
  enrichService: PostEnrichService,
  repo: PostRepository,
  implicit val ec: ExecutionContext
) extends PostService {

  override def hasSupported(supporter: UserID, url: Resource): Future[Boolean] = {
    getPostOrProject(url) map {
      case Left(post) => post.thank.isSupportedBy(supporter)
      case Right(_) => false
    }
  }

  override def findById(id: PostID): Future[Option[Post]] = {
    repo.findById(id)
  }

  override def delete(id: PostID): Future[Boolean] = {
    repo.delete(id).map({
      case Some(post) =>
        postEventBus.publish(PostRemoved(post))
        true
      case None =>
        true
    })
  }

  override def getPostOrProject(url: Resource): Future[Either[Post, Project]] = {
    def findProjectIfNoPost(postOpt: Option[Post]): Future[Either[Post, Project]] = {
      postOpt match {
        case Some(post) =>
          Future.successful(Left(post))
        case None =>
          lookupService
            .findByUrl(url)
            .map({
              case Some(prj) => Right(prj)
              case None => throw ResourceException.ownerMissing(url)
            })
      }
    }

    repo.findByResource(url).flatMap(findProjectIfNoPost)
  }


  override def findByTags(tags: Set[Tag]): Future[List[Post]] = {
    repo.findByTags(tags)
  }

  override def findByAuthor(author: UserID): Future[List[Post]] = {
    repo.findByAuthor(author)
  }

  override def findByProject(project: ProjectID): Future[List[Post]] = {
    repo.findByProject(project)
  }

  override def findLastByProject(project: ProjectID): Future[Option[Post]] = {
    repo.findLastByProject(project)
  }

  override def refresh(user: UserID, post: PostID): Future[Post] = {
    repo.findById(post).flatMap({
      case Some(Post(_, project, ogObj, _, _, _)) if project.user == user =>
        enrichService.enrich(project, ogObj).flatMap(create)
      case Some(_) =>
        Future.failed(ResourceException.ownershipNotVerified())
      case None =>
        Future.failed(ResourceException.noResourceExists())
    })
  }

  override def create(og: OpenGraphObject): Future[Post] = {
    val res = og.url
    getPostOrProject(res).flatMap({
      case Left(oldPost) =>
        val newPost = oldPost.withOg(og)
        val saveNewPost = repo.update(newPost).map(_.get)
        saveNewPost.map(PostUpdated(oldPost, _)).foreach(postEventBus.publish)
        saveNewPost
      case Right(project) =>
        val post = Post.from(og, project)
        val saveNewPost = repo.save(post).filter(_ == true).map((_) => post)
        saveNewPost.map(PostCreated).foreach(postEventBus.publish)
        saveNewPost
    })
  }

  override def updateProject(project: Project): Future[Boolean] = {
    repo.updateProject(project)
  }

  override def delete(project: Project): Future[Boolean] = {
    repo.
      findByProject(project._id).
      flatMap(posts => {
        posts.map(PostRemoved).foreach(postEventBus.publish(_))
        repo.deleteAll(project.user, project.url)
      })
  }

  override def thank(giver: UserID, url: Resource): Future[Post] = {
    getPostOrProject(url) flatMap {
      case Left(post) =>
        Future.successful(post)
      case Right(prj) =>
        enrichService
          .enrich(prj, OpenGraphObject(url = url))
          .flatMap(create)
    } flatMap (post => {
      if (post.project.user == giver)
        throw PaymentException.selfLovingIsForbidden()

      repo.markSupported(giver, url).map(increased => {
        if (increased) {
          thankEventBus.publish(ThankEvent(giver, post.project, url))
          post.copy(thank = post.thank.withSupporter(giver))
        } else {
          post
        }
      })
    })
  }

}