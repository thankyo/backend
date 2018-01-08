package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, ThankEvent, UserID}
import com.clemble.loveit.thank.model.{Post}
import com.clemble.loveit.thank.service.repository.PostRepository
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.error.ResourceException

import scala.concurrent.{ExecutionContext, Future}

trait PostService {

  def hasSupported(giver: UserID, uri: Resource): Future[Boolean]

  def getOrCreate(uri: Resource): Future[Post]

  def thank(giver: UserID, uri: Resource): Future[Post]

}

@Singleton
case class SimplePostService @Inject()(
                                         thankEventBus: ThankEventBus,
                                         thankRepo: PostRepository,
                                         implicit val ec: ExecutionContext
) extends PostService {

  override def hasSupported(supporter: UserID, res: Resource): Future[Boolean] = {
    thankRepo.isSupportedBy(supporter, res).flatMap(_ match {
      case Some(thanked) => Future.successful(thanked)
      case None => getOrCreate(res).map(post => post.isSupportedBy(supporter))
    })
  }

  override def getOrCreate(resource: Resource): Future[Post] = {
    def createIfMissing(postOpt: Option[Post]): Future[Post] = {
      postOpt match {
        case Some(post) => Future.successful(post)
        case None =>
          resource.parent() match {
            case Some(parRes) =>
              for {
                owner <- getOrCreate(parRes).map(_.project)
                post = Post(resource, owner)
                createdNew <- thankRepo.save(post)
                created <- if(createdNew) Future.successful(post) else thankRepo.findByResource(resource).map(_.get)
              } yield {
                created
              }
            case None => // TODO define proper error handling here
              throw ResourceException.ownerMissing()
          }
      }
    }

    thankRepo.findByResource(resource).flatMap(createIfMissing)
  }

  override def thank(giver: UserID, res: Resource): Future[Post] = {
    for {
      post <- getOrCreate(res) // Ensure Thank exists
      increased <- thankRepo.markSupported(giver, res)
    } yield {
      if (increased) {
        thankEventBus.publish(ThankEvent(giver, post.project, res))
      }
      post
    }
  }

}