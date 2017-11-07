package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.controller.{ControllerUtils, LoveItController}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.model.PendingTransaction
import com.clemble.loveit.payment.service.PendingTransactionService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
case class PendingTransactionController @Inject()(
                                                   pendindTransactionService: PendingTransactionService,
                                                   silhouette: Silhouette[AuthEnv],
                                                   components: ControllerComponents,
                                                   implicit val m: Materializer,
                                                   implicit val ec: ExecutionContext
                                                 ) extends LoveItController(components) {

  def list(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    val userID = ControllerUtils.idOrMe(user)
    val thanks = pendindTransactionService.list(userID)
    thanks.
      runWith(Sink.seq[PendingTransaction]).
      map(tr => Ok(Json.toJson(tr)))
  })

}
