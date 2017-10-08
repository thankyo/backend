package com.clemble.loveit.thank.controller

import java.time.YearMonth
import javax.inject.Inject

import com.clemble.loveit.common.controller.ControllerUtils._
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.thank.service.repository.UserStatRepo
import com.mohiva.play.silhouette.api.Silhouette
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

class UserStatController @Inject()(
                                    statRepo: UserStatRepo,
                                    silhouette: Silhouette[AuthEnv],
                                    components: ControllerComponents,
                                    implicit val ec: ExecutionContext
                                  ) extends AbstractController(components) {

  def get(supporter: UserID, year: Int, month: Int) = silhouette.SecuredAction.async(implicit req => {
    val user = idOrMe(supporter)
    val yearMonth = YearMonth.of(year, month)
    statRepo.get(user, yearMonth).map(stat => Ok(Json.toJson(stat)))
  })

}
