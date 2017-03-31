package com.clemble.loveit.model

import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json

/**
  * Thank abstraction
  */
case class Thank(
                  resource: Resource,
                  given: Amount = 0L,
                  created: DateTime = DateTime.now(DateTimeZone.UTC)
                ) extends CreatedAware {

  def withParents(): List[Thank] = {
    resource.parents().map(Thank(_))
  }

  override def equals(obj: scala.Any): Boolean = obj match {
    case Thank(resource, given, _) => resource == this.resource && given == this.given
    case _ => false
  }

}

object Thank {

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

}