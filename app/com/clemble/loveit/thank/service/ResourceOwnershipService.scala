package com.clemble.loveit.thank.service


import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Amount, Resource, UserID}
import com.clemble.loveit.thank.model.{Thank}
import com.clemble.loveit.user.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.service.repository.ThankRepository

import scala.concurrent.{ExecutionContext, Future}

trait ResourceOwnershipService {

  def list(user: UserID): Future[Set[Resource]]

  def findOwner(uri: Resource): Future[Option[User]]

  def assign(user: UserID, ownership: Resource):  Future[Resource]

  @deprecated
  def updateBalance(user: UserID, change: Amount): Future[Boolean]

}

@Singleton
case class SimpleResourceOwnershipService @Inject() (userRepo: UserRepository, thankRepo: ThankRepository, implicit val ec: ExecutionContext) extends ResourceOwnershipService {

  override def list(user: UserID): Future[Set[Resource]] = {
    userRepo.findById(user).map(_.map(_.owns).getOrElse(Set.empty))
  }

  override def assign(userId: UserID, resource: Resource): Future[Resource] = {
    for {
      ownerOpt <- userRepo.findOwner(resource)
      userOpt <- userRepo.findById(userId)
    } yield {
      val alreadyOwned = ownerOpt.map(_.id != userId).getOrElse(false)
      if (alreadyOwned) throw UserException.resourceAlreadyOwned(ownerOpt.get)
      if (userOpt.isEmpty) throw UserException.userMissing(userId)
      thankRepo.save(Thank(resource, userId))
      userRepo.update(userOpt.get.assignOwnership(resource))
      resource
    }
  }

  override def findOwner(uri: Resource): Future[Option[User]] = {
    userRepo.findOwner(uri)
  }

  // TODO this is duplication need to be unified and used only once
  @deprecated
  override def updateBalance(user: UserID, change: Amount): Future[Boolean] = {
    userRepo.changeBalance(user, change)
  }

}
