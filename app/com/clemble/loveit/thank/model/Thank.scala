package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Amount, CreatedAware, Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json

/**
  * Thank abstraction
  */
case class Thank(
                  resource: Resource,
                  owner: UserID,
                  given: Amount = 0L,
                  givers: Set[UserID] = Set.empty,
                  created: DateTime = DateTime.now(DateTimeZone.UTC)
                ) extends CreatedAware {

  def thankedBy(user: UserID) = givers.contains(user)

  override def equals(obj: scala.Any): Boolean = obj match {
    case Thank(resource, given, _, _ ,_) => resource == this.resource && given == this.given
    case _ => false
  }

}

object Thank {

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

  implicit val thankWriteable = WriteableUtils.jsonToWriteable[Thank]

}