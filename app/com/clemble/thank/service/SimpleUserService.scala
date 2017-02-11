package com.clemble.thank.service

import com.clemble.thank.model.{User, UserId}
import com.clemble.thank.service.repository.UserRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleUserService @Inject()(repository: UserRepository, implicit val ec: ExecutionContext) extends UserService {

  override def create(user: User): Future[User] = {
    repository.save(user)
  }

  override def get(id: UserId): Future[Option[User]] = {
    repository.findById(id)
  }

}
