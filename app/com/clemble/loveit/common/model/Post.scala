package com.clemble.loveit.common.model

import java.time.LocalDateTime

import com.clemble.loveit.common.util.{IDGenerator, WriteableUtils}
import play.api.libs.json.{Json, OFormat}

case class Post(
                 url: Resource,
                 project: Project,

                 ogObj: OpenGraphObject,

                 thank: Thank = Thank(),

                 created: LocalDateTime = LocalDateTime.now(),
                 _id: PostID = IDGenerator.generate()
               ) extends CreatedAware with ResourceAware {

  def isSupportedBy(user: UserID): Boolean = thank.isSupportedBy(user)

  def validate() = {
    if (!url.parents().contains(project.url)) {
      throw new IllegalArgumentException("URL should be a child of a project")
    }
  }

  def withOg(og: OpenGraphObject): Post = {
    this.copy(ogObj = og.copy(tags = og.tags ++ project.tags))
  }

  def withUrl(url: String): Post = {
    copy(url = url, ogObj = ogObj.copy(url = url))
  }
}

object Post {

  implicit val jsonFormat: OFormat[Post] = Json.format[Post]

  implicit val postWriteable = WriteableUtils.jsonToWriteable[Post]

  implicit val postsWriteable = WriteableUtils.jsonToWriteable[List[Post]]

  def from(url: Resource, project: Project): Post = {
    Post(url, project, OpenGraphObject(url = url, tags = project.tags))
  }

  def from(og: OpenGraphObject, project: Project): Post = {
    Post.from(og.url, project).withOg(og)
  }
}
