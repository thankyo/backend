package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.ResourceException
import com.clemble.loveit.common.model.{Resource, Tag, ThankEvent, UserID}
import com.clemble.loveit.thank.model.{OpenGraphObject, Post, SupportedProject}
import com.clemble.loveit.thank.service.repository.PostRepository

import scala.concurrent.{ExecutionContext, Future}

trait PostService {

  def hasSupported(giver: UserID, uri: Resource): Future[Boolean]

  def getOrCreate(uri: Resource): Future[Post]

  def create(og: OpenGraphObject): Future[Post]

  def assignTags(uri: Resource, tags: Set[Tag]): Future[Boolean]

  def updateOwner(owner: SupportedProject, url: Resource): Future[Boolean]

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
      case None => getOrCreate(res).map(post => post.isSupportedBy(supporter))
    })
  }

  override def getOrCreate(res: Resource): Future[Post] = {
    def createIfMissing(postOpt: Option[Post]): Future[Post] = {
      postOpt match {
        case Some(post) =>
          Future.successful(post)
        case None =>
          userResService
            .findOwner(res)
            .flatMap(ownerOpt => {
              ownerOpt match {
                case Some(owner) =>
                  val post = Post.from(res, owner)
                  postRepo.save(post).filter(_ == true).flatMap(_ => postRepo.findByResource(res).map(_.get))
                case None => {
                  throw ResourceException.ownerMissing()
                }
              }
            })
      }
    }

    postRepo.findByResource(res).flatMap(createIfMissing)
  }

  override def create(og: OpenGraphObject): Future[Post] = {
    val res = Resource.from(og.url)
    for {
      post <- getOrCreate(res)
      saved <- postRepo.update(post.withOg(og)) if (saved)
    } yield {
      post.withOg(og)
    }
  }

  override def assignTags(uri: Resource, tags: Set[Tag]): Future[Boolean] = {
    postRepo.assignTags(uri, tags)
  }

  override def updateOwner(owner: SupportedProject, res: Resource): Future[Boolean] = {
    val fPost = getOrCreate(res)
      .recoverWith({ case _ => {
        val post = Post.from(res, owner)
        postRepo.save(post).filter(_ == true).map(_ => post)
      }
      })
    fPost
      .flatMap(post => {
        postRepo.updateOwner(owner, res)
      })
  }

  override def thank(giver: UserID, res: Resource): Future[Post] = {
    for {
      post <- getOrCreate(res) // Ensure Thank exists
      increased <- postRepo.markSupported(giver, res)
    } yield {
      if (increased) {
        thankEventBus.publish(ThankEvent(giver, post.project, res))
      }
      post
    }
  }

}