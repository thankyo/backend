package com.clemble.loveit.payment.controller

import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.controller.{LoveItController}
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.PendingTransactionService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
case class PendingTransactionController @Inject()(
                                                   service: PendingTransactionService,
                                                   silhouette: Silhouette[AuthEnv],
                                                   components: ControllerComponents,
                                                   implicit val m: Materializer,
                                                   implicit val ec: ExecutionContext
                                                 ) extends LoveItController(components) {

  def listOutgoing(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.listOutgoing(idOrMe(user)).map(Ok(_))
  })

  def listIncoming(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.listIncoming(idOrMe(user)).map(Ok(_))
  })

}
