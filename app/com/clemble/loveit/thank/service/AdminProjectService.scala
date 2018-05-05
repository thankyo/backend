package com.clemble.loveit.thank.service

import com.clemble.loveit.thank.model.UserProject
import com.clemble.loveit.thank.service.repository.{UserProjectRepository}
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

trait AdminProjectService {

  def findAll(): Future[List[UserProject]]

}

@Singleton
case class SimpleAdminProjectService @Inject() (repo: UserProjectRepository) extends AdminProjectService {

  override def findAll(): Future[List[UserProject]] = {
    repo.findAll()
  }

}