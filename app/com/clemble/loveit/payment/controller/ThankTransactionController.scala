package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.controller.ControllerUtils
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ThankTransaction
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

@Singleton
case class ThankTransactionController @Inject()(
                                                 transactionService: ThankTransactionService,
                                                 silhouette: Silhouette[AuthEnv],
                                                 implicit val m: Materializer,
                                                 implicit val ec: ExecutionContext
) extends Controller {

  def list(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    val userID = ControllerUtils.idOrMe(user)
    val thanks = transactionService.list(userID)
    thanks.
      runWith(Sink.seq[ThankTransaction]).
      map(tr => Ok(Json.toJson(tr)))
  })

}
