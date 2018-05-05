package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.ProjectLike
import com.clemble.loveit.thank.model.UserProject
import com.clemble.loveit.thank.service.repository.ProjectRepository
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future

trait AdminProjectService {

  def findAll(): Future[List[ProjectLike]]

}

@Singleton
case class SimpleAdminProjectService @Inject() (repo: ProjectRepository) extends AdminProjectService {

  override def findAll(): Future[List[UserProject]] = {
    repo.findAll()
  }

}