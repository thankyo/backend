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

  def createOrUpdateUser(profile: CommonSocialProfile): Future[Either[UserIdentity, UserIdentity]] = {
    for {
      existingUserOpt <- userRepo.retrieve(profile.loginInfo)
      user <- existingUserOpt.
        map(identity => Future.successful(identity)).
        getOrElse(userRepo.save(User from profile).map(_.toIdentity()))
    } yield {
      if (existingUserOpt.isEmpty) {
        Left(user)
      } else {
        Right(user)
      }
    }
  }
}
