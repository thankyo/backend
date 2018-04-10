package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.{Project, UserID}
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

  val prjLookupService = dependency[ProjectLookupService]

  override def getProjectsByUser(user: UserID): List[Project] = {
    await(prjLookupService.findByUser(user))
  }

}
