package com.clemble.loveit.payment.controller

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.EOMPaymentService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc._

import scala.concurrent.ExecutionContext


class AdminEOMController @Inject()(
                                    service: EOMPaymentService,
                                    silhouette: Silhouette[AuthEnv],
                                    components: ControllerComponents,
                                    implicit val ec: ExecutionContext
                                  ) extends AbstractController(components) {

  def getStatus(yom: YearMonth): Action[AnyContent] = silhouette.SecuredAction.async(implicit req => {
    val fStatus = service.getStatus(yom)
    fStatus.map(_ match {
      case Some(status) => Ok(status)
      case None => NotFound
    })
  })

  def run(yom: YearMonth): Action[AnyContent] = silhouette.SecuredAction.async(implicit req => {
    val fStatus = service.run(yom)
    fStatus.map(Ok(_))
  })


}
