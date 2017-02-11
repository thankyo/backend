package com.clemble.thank.service.impl

import com.clemble.thank.model.{Amount, User, UserId}
import com.clemble.thank.service.UserService
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(repository: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def create(user: User): Future[User] = {
    repository.save(user)
  }

  override def get(id: UserId): Future[Option[User]] = {
    repository.findById(id)
  }

  override def updateBalance(user: UserId, change: Amount): Future[Boolean] = {
    repository.changeBalance(user, change)
  }

  override def updateOwnerBalance(uri: String, change: Amount): Future[Boolean] = {
    def createOwnerIfMissing(userOpt: Option[User]): Future[UserId] = {
      userOpt match {
        case Some(user) => Future.successful(user.id)
        case None => repository.save(User.empty(uri)).map(_.id)
      }
    }

    for {
      ownerOpt <- repository.findOwner(uri)
      ownerId <- createOwnerIfMissing(ownerOpt)
      update <- updateBalance(ownerId, change)
    } yield {
      update
    }
  }

}
