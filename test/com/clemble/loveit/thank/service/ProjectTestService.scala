package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.Project
import com.clemble.loveit.thank.service.repository.ProjectRepository

trait ProjectTestService {

  def getProjectsByUser(id: UserID): List[Project]

}

trait RepoProjectTestService extends ServiceSpec with ProjectTestService {

  override def getProjectsByUser(user: UserID): List[Project] = {
    await(prjRepo.findByUser(user))
  }

}

trait InternalProjectTestService extends ServiceSpec with ProjectTestService {

  val prjService = dependency[ProjectService]

  override def getProjectsByUser(user: UserID): List[Project] = {
    await(prjService.findProjectsByUser(user))
  }

}
