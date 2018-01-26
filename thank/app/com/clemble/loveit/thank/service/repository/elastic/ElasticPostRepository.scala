package com.clemble.loveit.thank.service.repository.elastic

import javax.inject.Inject

import com.clemble.loveit.common.model.{MimeType, Resource, Tag, UserID}
import com.clemble.loveit.thank.model._
import com.clemble.loveit.thank.service.repository.PostRepository
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.playjson._
import com.sksamuel.elastic4s.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

case class ElasticPostRepository @Inject()(client: HttpClient)(implicit ec: ExecutionContext) extends PostRepository {

  client.execute {
    createIndex("loveIt").mappings(
      mapping("post").fields(
        textField("resource"),
        objectField("project").fields(
          textField("id"),
          textField("firstName"),
          textField("lastName"),
          textField("avatar"),
          textField("bio"),
          keywordField("tags")
        ),
        objectField("ogObj").fields(
          textField("url"),
          textField("title"),
          textField("type"),
          objectField("image").fields(
            textField("url"),
            textField("secureUrl"),
            textField("imageType"),
            intField("width"),
            intField("height"),
            textField("alt")
          ),
          textField("description")
        ),
        keywordField("tags"),
        objectField("thank").fields(
          intField("given"),
          keywordField("supporters")
        ),
        dateField("created")
      )
    )
  }

  override def save(post: Post): Future[Boolean] = {
    val fIndexed = client.execute(
      indexInto("loveIt" / "post").doc[Post](post)
    )
    fIndexed.map(_.isRight)
  }

  override def assignTags(res: Resource, tags: Set[Tag]): Future[Boolean] = {
    ???
  }

  override def update(post: Post): Future[Boolean] = {
    ???
  }

  override def findByResource(uri: Resource): Future[Option[Post]] = {
    ???
  }

  override def findByTags(tag: Set[String]): Future[List[Post]] = ???

  override def updateOwner(owner: SupportedProject, url: Resource): Future[Boolean] = {
    ???
  }

  override def markSupported(user: String, url: Resource): Future[Boolean] = {
    ???
  }

  override def isSupportedBy(user: UserID, resource: Resource): Future[Option[Boolean]] = {
    ???
  }

}
