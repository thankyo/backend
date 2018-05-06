package com.clemble.loveit.user.service

import com.clemble.loveit.auth.service.UserRegister
import com.clemble.loveit.common.model.User
import com.clemble.loveit.user.service.repository.UserRepository
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

trait AdminUserService {

  def count(): Future[Int]

  def list(): Future[List[User]]

}

@Singleton
case class SimpleAdminUserService @Inject()(repo: UserRepository) extends AdminUserService {

  override def count(): Future[Int] = {
    repo.count()
  }

  override def list(): Future[List[User]] = {
    repo.findAll()
  }

}
