package com.clemble.loveit.thank.service

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{Post, ProjectConstructor, Tumblr, UserID}
import com.clemble.loveit.common.service._
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import javax.inject.Inject
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.libs.ws.{WSClient, WSSignatureCalculator}

import scala.concurrent.{ExecutionContext, Future}

trait TumblrIntegrationService {

  def integrateWithLoveIt(post: Post): Future[Boolean]

  def removeIntegration(post: Post): Future[Boolean]

}

case class TumblrIntegrationListener (integrationService: TumblrIntegrationService) extends Actor {

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

  userService: UserService,
  oAuthService: UserOAuthService,
  tumblrProvider: TumblrProvider,

  client: WSClient,
  userOAuth: UserOAuthService,
  implicit val ec: ExecutionContext
) extends TumblrIntegrationService {

  {
    val listener = actorSystem.actorOf(Props(TumblrIntegrationListener(this)))
    postEventBus.subscribe(listener, classOf[PostCreated])
    postEventBus.subscribe(listener, classOf[PostRemoved])
  }

  private def getSigner(user: UserID): Future[Option[WSSignatureCalculator]] = {
    for {
      userOpt <- userService.findById(user)
      tumblrLoginOpt = userOpt.flatMap(_.profiles.asTumblrLogin())
      tumblrAuthOpt <- tumblrLoginOpt.map(oAuthService.findAuthInfo).getOrElse(Future.successful(None))
    } yield {
      tumblrAuthOpt match {
        case Some(info: OAuth1Info) =>
          Some(tumblrProvider.service.sign(info))
        case _ =>
          None
      }
    }
  }

  private def getPostUrl(post: Post): String = {
    val parts = post.url.split("/")
    s"https://api.tumblr.com/v2/blog/${parts(2)}/posts?id=${parts(4)}"
  }

  private def updatePostUrl(post: Post): String = {
    val parts = post.url.split("/")
    s"https://api.tumblr.com/v2/blog/${parts(2)}/post/edit?id=${parts(4)}"
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

  private def addIntegrationUpdate(integration: String, post: JsObject): JsObject = {
    val oldCaption = (post \ "caption").asOpt[String].getOrElse("")
    Json.obj("caption" -> (oldCaption + integration), "type" -> "photo", "id" -> (post \ "id").as[JsValue])
  }

  override def integrateWithLoveIt(post: Post): Future[Boolean] = {
    for {
      signerOpt <- getSigner(post.project.user)
      signer = signerOpt.get
      existingPostOpt <- client.url(getPostUrl(post)).sign(signer).get().
        map(res => if (res.status == 200) Some((res.json \ "response" \ "posts").as[List[JsObject]].head) else None)
      existingPost = existingPostOpt.get
      addIntegration = addIntegrationUpdate(generateIntegration(post), existingPost)
      updated <- client.url(updatePostUrl(post)).sign(signer).post(addIntegration).map(res => res.json)
    } yield {
      println(updated)
      (updated \ "meta" \ "status").asOpt[Int] == Some(200)
    }
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

  private def removeIntegrationUpdate(post: JsObject): JsObject = {
    val caption = cleanCaption((post \ "caption").asOpt[String].getOrElse(""))
    Json.obj("caption" -> caption, "type" -> "photo", "id" -> (post \ "id").as[JsValue])
  }

  override def removeIntegration(post: Post): Future[Boolean] = {
    for {
      signerOpt <- getSigner(post.project.user)
      signer = signerOpt.get
      existingPostOpt <- client.url(getPostUrl(post)).sign(signer).get().
        map(res => if (res.status == 200) Some((res.json \ "response" \ "posts").as[List[JsObject]].head) else None)
      existingPost = existingPostOpt.get
      removeIntegrationPost = removeIntegrationUpdate(existingPost)
      updated <- client.url(updatePostUrl(post)).sign(signer).post(removeIntegrationPost).map(res => res.json)
    } yield {
      println(updated)
      (updated \ "meta" \ "status").asOpt[Int] == Some(200)
    }
  }

}