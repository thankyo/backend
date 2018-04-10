package com.clemble.loveit.user.controller

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.user.model.Invitation
import com.clemble.loveit.user.service.InvitationService
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.{Logger, Silhouette}
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import play.api.libs.json._
import play.api.mvc.{ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class InvitationController @Inject()(
                                           invitationService: InvitationService,
                                           silhouette: Silhouette[AuthEnv],
                                           authInfoRepository: AuthInfoRepository,
                                           socialProviderRegistry: SocialProviderRegistry,
                                           components: ControllerComponents,
                                           implicit val ec: ExecutionContext
                                         ) extends LoveItController(components) with Logger {

  def invite() = silhouette.SecuredAction.async(parse.json[JsObject].map(_ \ "linkOrEmail"))(implicit req => {
    req.body match {
      case JsDefined(JsString(linkOrEmail)) =>
        val inv = Invitation(linkOrEmail, req.identity.id)
        invitationService.
          save(inv).
          map(res => Ok(Json.toJson(res)))
      case _ =>
        Future.successful(BadRequest("linkOrEmail field is missing"))
    }
  })

}
