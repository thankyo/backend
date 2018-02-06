package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Amount, UserID}
import play.api.libs.json.Json

/**
  * Thank abstraction
  */
case class Thank(
                  given: Amount = 0L,
                  supporters: Set[UserID] = Set.empty,
                ) {


  def isSupportedBy(user: UserID): Boolean = supporters.contains(user)

  def withSupporter(supporter: UserID): Thank = {
    if (!supporters.contains(supporter)) {
      Thank(supporters = supporters + supporter, given = given + 1)
    } else {
      this
    }
  }

}

object Thank {

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

}