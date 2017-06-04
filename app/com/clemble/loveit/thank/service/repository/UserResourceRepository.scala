package com.clemble.loveit.thank.service.repository

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.UserResource

import scala.concurrent.Future

trait UserResourceRepository {

  def find(user: UserID): Future[Option[UserResource]]

}
