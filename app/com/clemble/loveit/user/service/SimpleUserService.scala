package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.{Email, UserID}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.service.UserService
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.util.IDGenerator
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(userRepo: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def create(user: User): Future[User] = {
    userRepo.save(user)
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    userRepo.retrieve(loginInfo)
  }

  override def findById(id: UserID): Future[Option[User]] = {
    userRepo.findById(id)
  }

  override def findByEmail(email: Email): Future[Option[User]] = {
    userRepo.findByEmail(email)
  }

  override def update(user: User): Future[User] = {
    userRepo.update(user)
  }

}
