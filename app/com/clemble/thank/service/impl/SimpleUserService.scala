package com.clemble.thank.service.impl

import com.clemble.thank.model.{Amount, ResourceOwnership, User, UserId}
import com.clemble.thank.service.{UserService}
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(repository: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def findById(id: UserId): Future[Option[User]] = {
    repository.findById(id)
  }

  override def assignOwnership(userId: UserId, resource: ResourceOwnership): Future[ResourceOwnership] = {
    def canOwn(resource: ResourceOwnership, users: List[User]): Boolean = {
      val allOwned = users.flatMap(_.owns)
      val oneOfAlreadyOwned = allOwned.contains(resource)
      val alreadyFullyOwned = allOwned.exists(_.owns(resource))
      !(oneOfAlreadyOwned || alreadyFullyOwned)
    }

    def toPendingBalance(resource: ResourceOwnership, relatedUsers: List[User]): Future[Amount] = {
      val realizedUsers = relatedUsers.filter(_.owns.exists(resource.owns))
      for {
        _ <- repository.remove(relatedUsers.map(_.id))
      } yield {
        realizedUsers.map(_.balance).sum
      }
    }
    for {
      relatedUsers <- repository.findRelated(resource) if (canOwn(resource, relatedUsers))
      userOpt <- repository.findById(userId) if (userOpt.isDefined && !relatedUsers.exists(_ == userOpt.get))
      user = userOpt.get
      pendingBalance <- toPendingBalance(resource, relatedUsers)
      user <- repository.update(user.assignOwnership(pendingBalance, resource))
    } yield {
      resource
    }
  }

  override def findResourceOwner(uri: String): Future[User] = {
    def createOwnerIfMissing(userOpt: Option[User]): Future[User] = {
      userOpt match {
        case Some(id) => Future.successful(id)
        case None => repository.save(User.empty(uri))
      }
    }

    def chooseOwner(uri: String): Future[Option[User]] = {
      val ownerships = ResourceOwnership.toPossibleOwnerships(uri)
      for {
        owners <- repository.findOwners(ownerships)
      } yield {
        val resToOwner = owners.
          flatMap(owner => {
            owner.owns.filter(ownerships.contains).map(res => res.uri -> owner)
          }).
          sortBy({ case (uri, _) => -uri.length })

        resToOwner.headOption.map({ case (_, owner) => owner })
      }
    }

    for {
      ownerOpt <- chooseOwner(uri)
      owner <- createOwnerIfMissing(ownerOpt)
    } yield {
      owner
    }
  }

  override def updateBalance(user: UserId, change: Amount): Future[Boolean] = {
    repository.changeBalance(user, change)
  }

  override def updateOwnerBalance(uri: String, change: Amount): Future[Boolean] = {
    for {
      owner <- findResourceOwner(uri)
      update <- updateBalance(owner.id, change)
    } yield {
      update
    }
  }

}
