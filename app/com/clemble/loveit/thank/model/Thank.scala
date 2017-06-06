package com.clemble.loveit.thank.model

import com.clemble.loveit.common.model.{Amount, CreatedAware, Resource, UserID}
import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
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
    case t : Thank =>
      t.resource == this.resource &&
      t.given == this.given &&
      t.owner == this.owner
    case _ => false
  }

}

object Thank {

  val INTEGRATION_DEFAULT = Thank(Resource.from("http://example.com/verified"), IDGenerator.ZERO)

  /**
    * JSON format for [[Thank]]
    */
  implicit val jsonFormat = Json.format[Thank]

  implicit val thankWriteable = WriteableUtils.jsonToWriteable[Thank]

}