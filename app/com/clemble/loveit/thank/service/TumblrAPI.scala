package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Post, UserID}
import com.clemble.loveit.common.service.{TumblrProvider, UserOAuthService, UserService, WSClientAware}
import com.mohiva.play.silhouette.impl.providers.OAuth1Info
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.libs.json.JsObject
import play.api.libs.ws.{WSClient, WSSignatureCalculator}

import scala.concurrent.{ExecutionContext, Future, Promise}

trait TumblrAPI {

  def findPost(user: String, blog: String, post: String): Future[Option[JsObject]]

  def findAndUpdatePost(user: String, blog: String, post: String, update: JsObject => Option[JsObject]): Future[Boolean]

}

@Singleton
case class SimpleTumblrAPI @Inject()(
  userService: UserService,
  oAuthService: UserOAuthService,
  tumblrProvider: TumblrProvider,

  client: WSClient,
  userOAuth: UserOAuthService,
  implicit val ec: ExecutionContext
) extends TumblrAPI with WSClientAware {

  val LOG = LoggerFactory.getLogger(classOf[TumblrAPI])

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

  override def findPost(user: String, blog: String, post: String): Future[Option[JsObject]] = {
    val promise = Promise[Option[JsObject]]
    findAndUpdatePost(user, blog, post, (post) => {
      promise success Some(post)
      None
    }).recover({ case t => promise failure t })
    promise.future
  }

  override def findAndUpdatePost(user: String, blog: String, post: String, update: JsObject => Option[JsObject]): Future[Boolean] = {
    for {
      signerOpt <- getSigner(user)
      signer = signerOpt.get
      existingPostOpt <- client.
        url(s"https://api.tumblr.com/v2/blog/${blog}/posts?id=${post}").
        sign(signer).
        get().
        map(res => if (res.status == 200) (res.json \ "response" \ "posts").as[List[JsObject]].headOption else None)
      change = existingPostOpt.flatMap(update(_))
      updated <- change.map(op =>
        client.
          url(s"https://api.tumblr.com/v2/blog/${blog}/post/edit?id=${post}").
          sign(signer).
          post(op).
          map(res => {
            val ok = (res.json \ "meta" \ "status").asOpt[Int].contains(200)
            if (!ok) {
              LOG.error(s"Error updating post ${res.json}")
            }
            ok
          })
      ).getOrElse(Future.successful(false))
    } yield {
      updated
    }
  }

}
