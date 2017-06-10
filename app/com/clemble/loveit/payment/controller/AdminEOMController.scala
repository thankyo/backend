package com.clemble.loveit.payment.controller

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.{ EOMService}
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.{Controller}

import scala.concurrent.ExecutionContext

class AdminEOMController @Inject()(
                                    service: EOMService,
                                    silhouette: Silhouette[AuthEnv],
                                    implicit val ec: ExecutionContext
                                  ) extends Controller {

  def getStatus(yom: YearMonth) = silhouette.SecuredAction.async(implicit req => {
    val fStatus = service.getStatus(yom)
    fStatus.map(_ match {
      case Some(status) => Ok(status)
      case None => NotFound
    })
  })

  def run(yom: YearMonth) = silhouette.SecuredAction.async(implicit req => {
    val fStatus = service.run(yom)
    fStatus.map(Ok(_))
  })


}
