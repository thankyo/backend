package com.clemble.loveit.thank.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Resource, UserID, _}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.{Json, OFormat}

case class Post(
                 resource: Resource,
                 project: SupportedProject,

                 ogObj: Option[OpenGraphObject] = None,
                 tags: Set[Tag] = Set.empty,

                 thank: Thank = Thank(),

                 created: LocalDateTime = LocalDateTime.now()
               ) extends CreatedAware {

  def isSupportedBy(user: UserID): Boolean = thank.isSupportedBy(user)

}

object Post {

  implicit val jsonFormat: OFormat[Post] = Json.format[Post]

  implicit val thankWriteable = WriteableUtils.jsonToWriteable[Post]


}
