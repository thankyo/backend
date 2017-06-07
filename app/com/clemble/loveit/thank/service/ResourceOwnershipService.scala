package com.clemble.loveit.thank.service


import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Resource, UserID}
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.service.repository.{ResourceRepository, ThankRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ResourceOwnershipService {

  def list(user: UserID): Future[Set[Resource]]

  def assignOwnership(user: UserID, res: Resource):  Future[Resource]

}

@Singleton
case class SimpleResourceOwnershipService @Inject() (userRepo: ResourceRepository, thankRepo: ThankRepository, implicit val ec: ExecutionContext) extends ResourceOwnershipService {

  override def list(user: UserID): Future[Set[Resource]] = {
    userRepo.listOwned(user)
  }

  override def assignOwnership(user: UserID, res: Resource): Future[Resource] = {
    // TODO assign is internal operation, so it might not need to throw Exception,
    // since verification has already been done before
    val fUpdate = for {
      ownerOpt <- userRepo.findOwner(res)
    } yield {
      if (ownerOpt.map(_ != user).getOrElse(false))
        throw UserException.resourceAlreadyOwned(ownerOpt.get)
      for {
        updThanks <- thankRepo.updateOwner(user, res) if(updThanks)
        updRes <- userRepo.assignOwnership(user, res) if(updRes)
      } yield {
        res
      }
    }
    fUpdate.flatMap(f => f)
  }

}
