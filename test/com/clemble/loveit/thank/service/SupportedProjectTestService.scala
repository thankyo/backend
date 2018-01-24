package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.SupportedProject
import com.clemble.loveit.thank.service.repository.SupportedProjectRepository

trait SupportedProjectTestService {

  def getProject(id: UserID): SupportedProject

}

trait RepoSupportedProjectTestService extends ServiceSpec with SupportedProjectTestService {

  val prjRepo = dependency[SupportedProjectRepository]

  override def getProject(user: UserID): SupportedProject = {
    await(prjRepo.getProject(user)).get
  }

}

trait InternalSupportedProjectTestService extends ServiceSpec with SupportedProjectTestService {

  val prjService = dependency[SupportedProjectService]

  override def getProject(user: UserID): SupportedProject = {
    await(prjService.getProject(user)).get
  }

}
