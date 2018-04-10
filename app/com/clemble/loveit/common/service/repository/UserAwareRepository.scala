package com.clemble.loveit.common.service.repository

import com.clemble.loveit.common.model.{UserAware, UserID}

import scala.concurrent.Future

trait UserAwareRepository[T <: UserAware] {

  /**
    * Find all entities related to the user
    *
    * @param user user identifier
    * @return all payments done by the user
    */
  def findByUser(user: UserID): Future[List[T]]

}
