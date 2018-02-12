package com.clemble.loveit.thank.model

import java.time.LocalDateTime

import com.clemble.loveit.common.model.{CreatedAware, Resource, UserID, _}
import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import play.api.libs.json.{Json, OFormat}

case class Post(
                 resource: Resource,
                 project: Project,

                 ogObj: OpenGraphObject,

                 thank: Thank = Thank(),

                 created: LocalDateTime = LocalDateTime.now(),
                 _id: String = IDGenerator.generate()
               ) extends CreatedAware with ResourceAware {

  def isSupportedBy(user: UserID): Boolean = thank.isSupportedBy(user)

  def validate() = {
    if (!resource.parents().contains(project.resource)) {
      throw new IllegalArgumentException("Resource should be a child of a project")
    }
  }

  def withOg(og: OpenGraphObject): Post = {
    this.copy(ogObj = og)
  }
}

object Post {

  implicit val jsonFormat: OFormat[Post] = Json.format[Post]

  implicit val postWriteable = WriteableUtils.jsonToWriteable[Post]

  implicit val postsWriteable = WriteableUtils.jsonToWriteable[List[Post]]

  def from(res: Resource, project: Project): Post = {
    Post(res, project, OpenGraphObject(url = res.stringify(), tags = project.tags))
  }

  def from(og: OpenGraphObject, project: Project): Post = {
    Post.from(Resource.from(og.url), project).withOg(og.copy(tags = og.tags ++ project.tags))
  }
}
