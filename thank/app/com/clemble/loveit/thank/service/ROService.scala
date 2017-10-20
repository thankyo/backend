package com.clemble.loveit.thank.service


import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Resource, UserID}
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.service.repository.{RORepository, ThankRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ROService {

  def list(user: UserID): Future[Set[Resource]]

  def assignOwnership(user: UserID, res: Resource):  Future[Resource]

}

@Singleton
case class SimpleResourceOwnershipService @Inject() (userRepo: RORepository, thankRepo: ThankRepository, implicit val ec: ExecutionContext) extends ROService {

  override def list(user: UserID): Future[Set[Resource]] = {
    userRepo.listOwned(user)
  }

  override def assignOwnership(user: UserID, res: Resource): Future[Resource] = {
    // TODO assign is internal operation, so it might not need to throw Exception,
    // since verification has already been done before
    val fUpdate = for {
      ownerOpt <- userRepo.findOwner(res)
    } yield {
      if (ownerOpt.exists(_ != user))
        throw UserException.resourceAlreadyOwned(ownerOpt.get)
      for {
        updThanks <- thankRepo.updateOwner(user, res)
        continue = if (updThanks) true else throw new IllegalArgumentException("Can't update owner for Thank")
        updRes <- userRepo.assignOwnership(user, res) if(continue)
      } yield {
        if (!updRes)
          throw new IllegalArgumentException("Can't assign ownership")
        res
      }
    }
    fUpdate.flatMap(f => f)
  }

}
