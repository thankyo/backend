package com.clemble.loveit.payment.controller

import java.time.YearMonth
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.model.PendingTransaction
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

  private def asCsv(transactions: List[PendingTransaction]): String = {
    val csv = transactions.map(transaction => {
      List(YearMonth.from(transaction.created), transaction.created, transaction.resource).mkString(",")
    })
    "Date,Time,URL\n" + csv.mkString("\n")
  }

  def getChargesCsv(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.listCharges(idOrMe(user))
      .map(asCsv)
      .map(Ok(_))
  })

  def listCharges(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.listCharges(idOrMe(user)).map(Ok(_))
  })

  def getPayoutsCsv(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.listPayouts(idOrMe(user)).map(asCsv).map(Ok(_))
  })

  def listPayouts(user: UserID) = silhouette.SecuredAction.async(implicit req => {
    service.listPayouts(idOrMe(user)).map(Ok(_))
  })

}
