package com.clemble.loveit.thank.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Resource, UserID}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.{Json, OFormat}

case class Post(
                 resource: Resource,
                 project: SupportedProject,
                 image: Option[Resource] = None,
                 description: Option[String] = None,
                 title: Option[String] = None,

                 thank: Thank = Thank(),
                 tags: List[String] = List.empty,

                 created: LocalDateTime = LocalDateTime.now()
               ) extends CreatedAware {

  def isSupportedBy(user: UserID): Boolean = thank.isSupportedBy(user)

}

object Post {

  implicit val jsonFormat: OFormat[Post] = Json.format[Post]

  implicit val thankWriteable = WriteableUtils.jsonToWriteable[Post]


}
