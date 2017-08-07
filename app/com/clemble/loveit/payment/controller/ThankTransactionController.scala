package com.clemble.loveit.payment.controller

import com.clemble.loveit.payment.service.ThankTransactionService
import com.clemble.loveit.common.util.AuthEnv
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
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

  def listMy() = silhouette.SecuredAction.async(implicit req => {
    val thanks = transactionService.list(req.identity.id)
    thanks.
      runWith(Sink.seq[ThankTransaction]).
      map(tr => Ok(Json.toJson(tr)))
  })

}
