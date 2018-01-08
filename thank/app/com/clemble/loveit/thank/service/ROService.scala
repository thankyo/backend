package com.clemble.loveit.thank.service


import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.{Resource, UserID}
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.thank.service.repository.{RORepository, PostRepository}

import scala.concurrent.{ExecutionContext, Future}

trait ROService {

  def list(user: UserID): Future[Set[Resource]]

  def assignOwnership(user: UserID, res: Resource):  Future[Resource]

}

@Singleton
case class SimpleResourceOwnershipService @Inject() (userRepo: RORepository, supportedProjectService: SupportedProjectService, thankRepo: PostRepository, implicit val ec: ExecutionContext) extends ROService {

  override def list(user: UserID): Future[Set[Resource]] = {
    userRepo.listOwned(user)
  }

  override def assignOwnership(owner: UserID, res: Resource): Future[Resource] = {
    // TODO assign is internal operation, so it might not need to throw Exception,
    // since verification has already been done before
    val fUpdate = for {
      ownerOpt <- userRepo.findOwner(res)
    } yield {
      if (ownerOpt.exists(_ != owner))
        throw UserException.resourceAlreadyOwned(ownerOpt.get)
      for {
        projectOpt <- supportedProjectService.getProject(owner)
        project = projectOpt.getOrElse({ throw new IllegalArgumentException("No project exists for the user")})
        updThanks <- thankRepo.updateOwner(project, res)
        continue = if (updThanks) true else throw new IllegalArgumentException("Can't update owner for Thank")
        updRes <- userRepo.assignOwnership(owner, res) if(continue)
      } yield {
        if (!updRes)
          throw new IllegalArgumentException("Can't assign ownership")
        res
      }
    }
    fUpdate.flatMap(f => f)
  }

}
