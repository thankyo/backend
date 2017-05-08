package com.clemble.loveit.thank.controller

import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.model.ResourceOwnership
import com.clemble.loveit.thank.service.{OwnershipVerificationGenerator, OwnershipVerificationService}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

case class OwnershipVerificationController @Inject()(
                                                      service: OwnershipVerificationService,
                                                      generator: OwnershipVerificationGenerator,
                                                      silhouette: Silhouette[AuthEnv],
                                                      implicit val ec: ExecutionContext
                                                    ) extends Controller {

  def listMy = silhouette.SecuredAction.async(implicit req => {
    val fOwned = service.list(req.identity.id)
    fOwned.map(Ok(_))
  })

  def verifyOwnership() = silhouette.SecuredAction.async(parse.json[ResourceOwnership])(implicit req => {
    val verificationReq = generator.generate(req.identity.id, req.body)
    val fVerification = service.verify(verificationReq)
    fVerification.map(Created(_))
  })

}
