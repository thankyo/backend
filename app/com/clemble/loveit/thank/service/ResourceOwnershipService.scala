package com.clemble.loveit.thank.service

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.thank.model.ResourceOwnership
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

trait ResourceOwnershipService {

  def listMy(user: UserID): Source[ResourceOwnership, _]

  def findResourceOwner(uri: Resource): Future[User]

  def assign(user: UserID, ownership: ResourceOwnership):  Future[ResourceOwnership]

  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}

@Singleton
case class SimpleResourceOwnershipService @Inject() (repository: UserRepository, implicit val ec: ExecutionContext) extends ResourceOwnershipService {

  override def listMy(user: UserID): Source[ResourceOwnership, _] = {
    val ownedResources = repository.findById(user).map(_.map(_.owns).getOrElse(Set.empty))
    Source.fromFuture(ownedResources).mapConcat(identity)
  }

  override def assign(userId: UserID, ownership: ResourceOwnership): Future[ResourceOwnership] = {
    def canOwn(users: List[User]): Boolean = {
      val allOwned = users.flatMap(_.owns)
      val oneOfAlreadyOwned = allOwned.contains(ownership)
      val alreadyFullyOwned = allOwned.exists(_.owns(ownership.resource))
      !(oneOfAlreadyOwned || alreadyFullyOwned)
    }

    def toPendingBalance(relatedUsers: List[User]): Future[Amount] = {
      val realizedUsers = relatedUsers.filter(_.owns.map(_.resource).exists(ownership.owns))
      for {
        _ <- repository.remove(relatedUsers.map(_.id))
      } yield {
        realizedUsers.map(_.balance).sum
      }
    }

    for {
      ownerOpt <- chooseOwner(ownership.resource)
      relatedUsers <- repository.findRelated(ownership)
      pendingBalance <- toPendingBalance(relatedUsers)
      userOpt <- repository.findById(userId)
    } yield {
      if (ownerOpt.isDefined)
        throw UserException.resourceAlreadyOwned(ownerOpt.get)
      if (!canOwn(relatedUsers) || userOpt.isEmpty || relatedUsers.exists(_ == userOpt.get))
        throw UserException.resourceOwnershipImpossible()
      repository.update(userOpt.get.assignOwnership(pendingBalance, ownership))
      ownership
    }
  }

  private def chooseOwner(uri: Resource): Future[Option[User]] = {
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

  override def findResourceOwner(uri: Resource): Future[User] = {
    def createOwnerIfMissing(userOpt: Option[User]): Future[User] = {
      userOpt match {
        case Some(id) => Future.successful(id)
        case None => repository.save(User.empty(uri))
      }
    }

    for {
      ownerOpt <- chooseOwner(uri)
      owner <- createOwnerIfMissing(ownerOpt)
    } yield {
      owner
    }
  }

  // TODO this is duplication need to be unified and used only once
  override def updateBalance(user: UserID, change: Amount): Future[Boolean] = {
    repository.changeBalance(user, change)
  }

}
