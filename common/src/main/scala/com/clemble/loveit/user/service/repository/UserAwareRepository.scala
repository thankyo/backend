package com.clemble.loveit.user.service.repository

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.user.model.UserAware

trait UserAwareRepository[T <: UserAware] {

  /**
    * Find all entities related to the user
    *
    * @param user user identifier
    * @return all payments done by the user
    */
  def findByUser(user: UserID): Source[T, _]

}
