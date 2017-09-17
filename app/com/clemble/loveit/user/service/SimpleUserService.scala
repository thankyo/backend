package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.{UserID}
import com.clemble.loveit.user.model._
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(userRepo: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def findById(id: UserID): Future[Option[User]] = {
    userRepo.findById(id)
  }

  def updateExistingUser(user: User, profile: CommonSocialProfile): Future[User] = {
    userRepo.save(user.link(profile))
  }

  def createOrUpdateUser(profile: CommonSocialProfile): Future[Either[UserIdentity, UserIdentity]] = {
    for {
      existingUserOpt <- userRepo.retrieve(profile.loginInfo)
      user <- existingUserOpt match {
        case Some(user: User) => userRepo.save(user link profile)
        case _ => userRepo.save(User from profile)
      }
    } yield {
      val identity = user.toIdentity();
      if (existingUserOpt.isDefined) {
        Left(identity)
      } else {
        Right(identity)
      }
    }
  }
}
