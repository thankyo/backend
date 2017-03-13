package com.clemble.thank.service.impl

import com.clemble.thank.model.{Amount, ResourceOwnership, User, UserId}
import com.clemble.thank.service.UserService
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(repository: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def updateBalance(user: UserId, change: Amount): Future[Boolean] = {
    repository.changeBalance(user, change)
  }

  override def updateOwnerBalance(uri: String, change: Amount): Future[Boolean] = {
    def createOwnerIfMissing(userOpt: Option[String]): Future[UserId] = {
      userOpt match {
        case Some(id) => Future.successful(id)
        case None => repository.save(User.empty(uri)).map(_.id)
      }
    }

    def chooseOwner(uri: String): Future[Option[UserId]] = {
      val ownerships = ResourceOwnership.toPossibleOwnerships(uri)
      for {
        owners <- repository.findOwners(ownerships)
      } yield {
        val resToOwner = owners.
          flatMap(owner => {
            owner.owns.filter(ownerships.contains).map(res => res.uri -> owner.id)
          }).
          sortBy({ case (uri, _) => -uri.length })

        resToOwner.headOption.map({ case (_, userId) => userId })
      }
    }

    for {
      ownerOpt <- chooseOwner(uri)
      ownerId <- createOwnerIfMissing(ownerOpt)
      update <- updateBalance(ownerId, change)
    } yield {
      update
    }
  }

}
