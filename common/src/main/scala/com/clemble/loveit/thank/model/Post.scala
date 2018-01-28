package com.clemble.loveit.thank.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Resource, UserID, _}
import com.clemble.loveit.common.util.WriteableUtils
import play.api.libs.json.{Json, OFormat}

case class Post(
                 resource: Resource,
                 project: SupportedProject,

                 ogObj: OpenGraphObject,
                 tags: Set[Tag] = Set.empty,

                 thank: Thank = Thank(),

                 created: LocalDateTime = LocalDateTime.now()
               ) extends CreatedAware {

  def isSupportedBy(user: UserID): Boolean = thank.isSupportedBy(user)

  def withOg(og: OpenGraphObject): Post = {
    this.copy(ogObj = og)
  }
}

object Post {

  implicit val jsonFormat: OFormat[Post] = Json.format[Post]

  implicit val postWriteable = WriteableUtils.jsonToWriteable[Post]

  implicit val postsWriteable = WriteableUtils.jsonToWriteable[List[Post]]

  def from(res: Resource, project: SupportedProject): Post = {
    Post(res, project, OpenGraphObject(res.stringify()), project.tags)
  }
}
