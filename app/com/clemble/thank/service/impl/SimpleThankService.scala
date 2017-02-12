package com.clemble.thank.service.impl

import com.clemble.thank.model.{Thank, UserId}
import com.clemble.thank.service.{ThankService, UserService}
import com.clemble.thank.service.repository.ThankRepository
import com.clemble.thank.util.URIUtils
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleThankService @Inject()(userService: UserService, repository: ThankRepository, implicit val ec: ExecutionContext) extends ThankService {

  override def get(uri: String): Future[Thank] = {
    def createIfMissing(thankOpt: Option[Thank]): Future[Thank] = {
      thankOpt match {
        case Some(thank) => Future.successful(thank)
        case None => repository.
          save(Thank(uri)).
          flatMap(_ => repository.findByURI(uri).map(_.get))
      }
    }

    repository.findByURI(uri).flatMap(createIfMissing)
  }

  override def thank(user: UserId, uri: String): Future[Thank] = {
    val normUri: String = URIUtils.normalize(uri)
    for {
      _ <- get(normUri) // Ensure Thank exists
      _ <- userService.updateBalance(user, -1)
      _ <- userService.updateOwnerBalance(normUri, 1)
      _ <- repository.increase(normUri)
      updated <- repository.findByURI(normUri).map(_.get)
    } yield {
      updated
    }
  }

}
