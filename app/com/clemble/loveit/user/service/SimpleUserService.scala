package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model._
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.util.IDGenerator
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(userRepo: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def save(user: User): Future[User] = {
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
    val email = profile.email.get
    val user = User(id = IDGenerator.generate(), email = email).link(profile)
    for {
      existingUserOpt <- userRepo.retrieve(profile.loginInfo)
      user <- existingUserOpt match {
        case Some(user: User) => userRepo.update(user link profile)
        case _ => userRepo.save(user)
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
