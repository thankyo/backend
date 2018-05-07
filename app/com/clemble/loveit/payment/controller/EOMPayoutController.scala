package com.clemble.loveit.payment.controller

import java.time.YearMonth
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.controller.LoveItController
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.service.EOMPayoutService
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc.ControllerComponents

import scala.concurrent.ExecutionContext

@Singleton
case class EOMPayoutController @Inject()(
                                          service: EOMPayoutService,
                                          silhouette: Silhouette[AuthEnv],
                                          components: ControllerComponents
                                        )(
                                          implicit ec: ExecutionContext
                                        ) extends LoveItController(silhouette, components) {

  def getMyCSV() = silhouette.SecuredAction.async(req => {
    val fCSV = for {
      payouts <- service.findByUser(req.identity.id)
    } yield {
      val csv = payouts.
        flatMap(payout => payout.transactions.map(transaction => {
          List(YearMonth.from(transaction.created), transaction.created, payout.status, transaction.url).mkString(",")
        })
      )

      "Date,Time,Payout Status,URL\n" + csv.mkString("\n")
    }

    fCSV.map(Ok(_))
  })

  def listMy() = silhouette.SecuredAction.async(req => {
    service.findByUser(req.identity.id).map(Ok(_))
  })

}
