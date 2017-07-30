package com.clemble.loveit.thank.controller

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.repository.UserStatRepo
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext

class UserStatController @Inject()(
                                    statRepo: UserStatRepo,
                                    silhouette: Silhouette[AuthEnv],
                                    implicit val ec: ExecutionContext
                                  ) extends Controller {

  def getMy(year: Int, month: Int) = silhouette.SecuredAction.async(implicit req => {
    val user = req.identity.id;
    val yearMonth = YearMonth.of(year, month)
    statRepo.get(req.identity.id, yearMonth).map(stat => Ok(Json.toJson(stat)))
  })

}
