package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.user.model._
import com.clemble.loveit.payment.model.BankDetails
import com.clemble.loveit.user.service.repository.UserRepository
import com.clemble.loveit.thank.model.ResourceOwnership
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(repository: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def findById(id: UserID): Future[Option[User]] = {
    repository.findById(id)
  }

  override def assignOwnership(userId: UserID, resource: ResourceOwnership): Future[ResourceOwnership] = {
    def canOwn(ownership: ResourceOwnership, users: List[User]): Boolean = {
      val allOwned = users.flatMap(_.owns)
      val oneOfAlreadyOwned = allOwned.contains(ownership)
      val alreadyFullyOwned = allOwned.exists(_.owns(ownership.resource))
      !(oneOfAlreadyOwned || alreadyFullyOwned)
    }

    def toPendingBalance(ownership: ResourceOwnership, relatedUsers: List[User]): Future[Amount] = {
      val realizedUsers = relatedUsers.filter(_.owns.map(_.resource).exists(ownership.owns))
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

  override def findResourceOwner(uri: Resource): Future[User] = {
    def createOwnerIfMissing(userOpt: Option[User]): Future[User] = {
      userOpt match {
        case Some(id) => Future.successful(id)
        case None => repository.save(User.empty(uri))
      }
    }

    def chooseOwner(uri: Resource): Future[Option[User]] = {
      val ownerships = ResourceOwnership.toPossibleOwnerships(uri)
      for {
        owners <- repository.findOwners(ownerships)
      } yield {
        val resToOwner = owners.
          flatMap(owner => {
            owner.owns.filter(ownerships.contains).map(res => res.resource -> owner)
          }).
          sortBy({ case (resource, _) => - resource.uri.length })

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

  override def setBankDetails(user: UserID, bankDetails: BankDetails): Future[Boolean] = {
    repository.setBankDetails(user, bankDetails)
  }

  override def updateBalance(user: UserID, change: Amount): Future[Boolean] = {
    repository.changeBalance(user, change)
  }

}
