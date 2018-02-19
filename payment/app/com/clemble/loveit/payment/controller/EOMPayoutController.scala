package com.clemble.loveit.payment.controller

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
                                        ) extends LoveItController(components) {

  def getMyCSV() = silhouette.SecuredAction.async(req => {
    val fCSV = for {
      payouts <- service.findByUser(req.identity.id)
    } yield {
      val csv = payouts.
        flatMap(payout => payout.
        transactions.
        groupBy(_.resource).
        map({
          case (resource, transactions) =>
            List(payout.yom.toString, transactions.size, resource.stringify()).
              mkString(",")
        })
      )

      csv.mkString("\n")
    }

    fCSV.map(Ok(_))
  })

  def listMy() = silhouette.SecuredAction.async(req => {
    service.findByUser(req.identity.id).map(Ok(_))
  })

}
