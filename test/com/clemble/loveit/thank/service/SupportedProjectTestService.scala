package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository

trait SupportedProjectTestService {

  def getProjectsByUser(id: UserID): List[SupportedProject]

}

trait RepoSupportedProjectTestService extends ServiceSpec with SupportedProjectTestService {

  val prjRepo = dependency[SupportedProjectRepository]

  override def getProjectsByUser(user: UserID): List[SupportedProject] = {
    await(prjRepo.getProjectsByUser(user))
  }

}

trait InternalSupportedProjectTestService extends ServiceSpec with SupportedProjectTestService {

  val prjService = dependency[SupportedProjectService]

  override def getProjectsByUser(user: UserID): List[SupportedProject] = {
    await(prjService.getProjectsByUser(user))
  }

}
