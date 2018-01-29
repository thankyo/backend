package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.model.{Resource, Tag, ThankEvent, UserID}
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, SupportedProject}
import com.clemble.loveit.thank.service.repository.PostRepository

import scala.concurrent.{ExecutionContext, Future}

trait PostService {

  def getPostOrProject(uri: Resource): Future[Either[Post, SupportedProject]]

  def create(og: OpenGraphObject): Future[Post]

  def assignTags(uri: Resource, tags: Set[Tag]): Future[Boolean]

  def updateOwner(owner: SupportedProject, url: Resource): Future[Boolean]

  def findByTags(tags: Set[Tag]): Future[List[Post]]

  def findByAuthor(author: UserID): Future[List[Post]]

  def hasSupported(giver: UserID, uri: Resource): Future[Boolean]

  def thank(supporter: UserID, url: Resource): Future[Post]

}

@Singleton
case class SimplePostService @Inject()(
                                        thankEventBus: ThankEventBus,
                                        userResService: UserResourceService,
                                        postRepo: PostRepository,
                                        implicit val ec: ExecutionContext
                                      ) extends PostService {

  override def hasSupported(supporter: UserID, res: Resource): Future[Boolean] = {
    postRepo.isSupportedBy(supporter, res).flatMap(_ match {
      case Some(thanked) => Future.successful(thanked)
      case None => Future.successful(false)
    })
  }

  override def getPostOrProject(res: Resource): Future[Either[Post, SupportedProject]] = {
    def createIfMissing(postOpt: Option[Post]): Future[Either[Post, SupportedProject]] = {
      postOpt match {
        case Some(post) =>
          Future.successful(Left(post))
        case None =>
          userResService
            .findOwner(res)
            .map(_ match {
                case Some(owner) => Right(owner)
                case None => throw ResourceException.ownerMissing()
              })
      }
    }

    postRepo.findByResource(res).flatMap(createIfMissing)
  }


  override def findByTags(tags: Set[Tag]): Future[List[Post]] = {
    postRepo.findByTags(tags)
  }

  override def findByAuthor(author: UserID): Future[List[Post]] = {
    postRepo.findByAuthor(author)
  }

  override def create(og: OpenGraphObject): Future[Post] = {
    val res = Resource.from(og.url)
    getPostOrProject(res).flatMap(_ match {
      case Left(post) =>
        val updPost = post.withOg(og)
        postRepo.update(updPost).filter(_ == true).map(_ => post)
      case Right(project) =>
        val post = Post.from(og, project)
        postRepo.save(post).filter(_ == true).map((_) => post)
    })
  }

  override def assignTags(uri: Resource, tags: Set[Tag]): Future[Boolean] = {
    postRepo.assignTags(uri, tags)
  }

  override def updateOwner(owner: SupportedProject, res: Resource): Future[Boolean] = {
    postRepo.updateOwner(owner, res)
  }

  override def thank(giver: UserID, res: Resource): Future[Post] = {
    for {
      postOpt <- postRepo.findByResource(res) // Ensure Thank exists
      post = postOpt.get
      increased <- postRepo.markSupported(giver, res)
    } yield {
      if (increased) {
        thankEventBus.publish(ThankEvent(giver, post.project, res))
      }
      post
    }
  }

}