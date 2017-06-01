package com.clemble.loveit.thank.service


import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.Thank
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.service.repository.{ResourceRepository, ThankRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ResourceOwnershipService {

  def list(user: UserID): Future[Set[Resource]]

  def assign(user: UserID, ownership: Resource):  Future[Resource]

}

@Singleton
case class SimpleResourceOwnershipService @Inject() (userRepo: ResourceRepository, thankRepo: ThankRepository, implicit val ec: ExecutionContext) extends ResourceOwnershipService {

  override def list(user: UserID): Future[Set[Resource]] = {
    userRepo.listOwned(user)
  }

  override def assign(user: UserID, resource: Resource): Future[Resource] = {
    // TODO assign is internal operation, so it might not need to throw Exception,
    // since verification has already been done before
    for {
      ownerOpt <- userRepo.findOwner(resource)
    } yield {
      val alreadyOwned = ownerOpt.map(_ != user).getOrElse(false)
      if (alreadyOwned) throw UserException.resourceAlreadyOwned(ownerOpt.get)
      thankRepo.save(Thank(resource, user))
      userRepo.assignOwnership(user, resource)
      resource
    }
  }

}
