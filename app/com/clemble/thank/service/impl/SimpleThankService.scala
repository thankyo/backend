package com.clemble.thank.service.impl

import com.clemble.thank.model.{Thank, UserId}
import com.clemble.thank.service.{ThankService, UserService}
import com.clemble.thank.service.repository.ThankRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleThankService @Inject()(userService: UserService, repository: ThankRepository, implicit val ec: ExecutionContext) extends ThankService {

  override def get(url: String): Future[Thank] = {
    def blankThank(url: String): Thank = Thank(url, 0)

    def createIfMissing(thankOpt: Option[Thank]): Future[Thank] = {
      thankOpt match {
        case Some(thank) => Future.successful(thank)
        case None => repository.save(blankThank(url)).flatMap(_ => repository.findByURI(url).map(_.get))
      }
    }

    repository.findByURI(url).flatMap(createIfMissing)
  }

  override def thank(user: UserId, uri: String): Future[Thank] = {
    for {
      _ <- get(uri) // Ensure Thank exists
      _ <- userService.updateBalance(user, -1)
      _ <- userService.updateOwnerBalance(uri, 1)
      _ <- repository.increase(uri)
      updated <- repository.findByURI(uri).map(_.get)
    } yield {
      updated
    }
  }

}
