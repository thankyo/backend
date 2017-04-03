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
                  given: Amount = 0L,
                  owner: Option[UserID] = None,
                  created: DateTime = DateTime.now(DateTimeZone.UTC)
                ) extends CreatedAware {

  def withParents(): List[Thank] = {
    resource.parents().map(Thank(_))
  }

  def setOwner(user: UserID): Thank = {
    this.copy(owner = Some(user))
  }

  def inc(): Thank = {
    this.copy(given = given + 1)
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case Thank(resource, given, _ ,_) => resource == this.resource && given == this.given
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