package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.{ Resource, UserID }
import com.clemble.loveit.thank.model.{ SupportedProject, UserResource }

import scala.concurrent.Future

trait UserResourceRepository {

  def save(uRes: UserResource): Future[Boolean]

  def find(user: UserID): Future[Option[UserResource]]

}
