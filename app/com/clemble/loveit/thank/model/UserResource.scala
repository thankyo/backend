package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Resource, UserID}

/**
  * Model required for UserResource
  */
trait UserResource {
  val id: UserID
  val owns: Set[Resource]
  val ownRequests: Set[ROVerificationRequest[Resource]]
}
