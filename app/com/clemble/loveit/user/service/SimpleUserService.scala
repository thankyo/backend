package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model._
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(userRepo: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def save(user: User) = {
    userRepo.save(user)
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    userRepo.retrieve(loginInfo)
  }

  override def findById(id: UserID): Future[Option[User]] = {
    userRepo.findById(id)
  }

  def updateExistingUser(user: User, profile: CommonSocialProfile): Future[User] = {
    userRepo.save(user.link(profile))
  }

  def createOrUpdateUser(profile: CommonSocialProfile): Future[Either[User, User]] = {
    for {
      existingUserOpt <- userRepo.retrieve(profile.loginInfo)
      user <- existingUserOpt match {
        case Some(user: User) => userRepo.save(user link profile)
        case Some(identity) =>
          // TODO this is not optimal additional call to DB, need to make it better
          userRepo.
            findById(identity.id).
            flatMap(userOpt => userRepo.update(userOpt.get link profile))
        case _ => userRepo.save(User from profile)
      }
    } yield {
      if (existingUserOpt.isDefined) {
        Left(user)
      } else {
        Right(user)
      }
    }
  }
}
