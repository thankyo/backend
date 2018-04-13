package com.clemble.loveit.thank.service

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{Post, Tumblr}
import com.clemble.loveit.common.service._
import javax.inject.Inject
import play.api.libs.json.{JsObject, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}

trait TumblrIntegrationService {

  def integrateWithLoveIt(post: Post): Future[Boolean]

  def removeIntegration(post: Post): Future[Boolean]

}

case class TumblrIntegrationListener(integrationService: TumblrIntegrationService) extends Actor {

  override def receive: Receive = {
    case PostCreated(post) if post.project.webStack == Some(Tumblr) =>
      integrationService.integrateWithLoveIt(post)
    case PostRemoved(post) if post.project.webStack == Some(Tumblr) =>
      integrationService.removeIntegration(post)
    case _ =>
  }

}

case class SimpleTumblrIntegrationService @Inject()(
  actorSystem: ActorSystem,
  postEventBus: PostEventBus,

  tumblrAPI: TumblrAPI,

  implicit val ec: ExecutionContext
) extends TumblrIntegrationService {

  {
    val listener = actorSystem.actorOf(Props(TumblrIntegrationListener(this)))
    postEventBus.subscribe(listener, classOf[PostCreated])
    postEventBus.subscribe(listener, classOf[PostRemoved])
  }

  private def generateIntegration(post: Post): String = {
    val parts = post.url.split("/")
    s"""
           <iframe
              src="https://loveit.tips/integration?post=${parts(4)}"
              width="80"
              height="100"
              class="like_toggle"
              allowtransparency="true"
              frameBorder="0">
            </iframe>
    """
  }

  private def addIntegrationUpdate(post: Post): JsObject => Option[JsObject] = {
    (tumblrPost: JsObject) => {
      val integration = generateIntegration(post)
      val oldCaption = (tumblrPost \ "caption").asOpt[String].getOrElse("")
      if (oldCaption.indexOf("<iframe") == -1) {
        Some(
          Json.obj("caption" -> (oldCaption + integration), "type" -> "photo", "id" -> (tumblrPost \ "id").as[JsValue])
        )
      } else {
        None
      }
    }
  }

  override def integrateWithLoveIt(post: Post): Future[Boolean] = {
    val parts = post.url.split("/")
    val blog = parts(2)
    val postId = parts(4)

    tumblrAPI.findAndUpdatePost(post.project.user, blog, postId, addIntegrationUpdate(post))
  }

  private def cleanCaption(caption: String): String = {
    val iFrameStart = caption.indexOf("<iframe")
    if (iFrameStart == -1) {
      caption
    } else {
      val iFrameEnd = caption.indexOf("/iframe>", iFrameStart) + 8
      cleanCaption(caption.substring(0, iFrameStart) + caption.substring(iFrameEnd))
    }
  }

  private def removeIntegrationUpdate(post: Post): JsObject => Option[JsObject] = {
    (tumblrPost: JsObject) => {
      val origCaption = (tumblrPost \ "caption").asOpt[String].getOrElse("")
      val caption = cleanCaption((tumblrPost \ "caption").asOpt[String].getOrElse(""))
      if (origCaption == caption) {
        None
      } else {
        Some(
          Json.obj("caption" -> caption, "type" -> "photo", "id" -> (tumblrPost \ "id").as[JsValue])
        )
      }
    }
  }

  override def removeIntegration(post: Post): Future[Boolean] = {
    val parts = post.url.split("/")
    val blog = parts(2)
    val postId = parts(4)

    tumblrAPI.findAndUpdatePost(post.project.user, blog, postId, removeIntegrationUpdate(post))
  }

}