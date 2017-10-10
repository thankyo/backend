package com.clemble.loveit.thank.service.repository

import java.time.YearMonth

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.{Thank, UserStat}

import scala.concurrent.Future

trait UserStatRepo {

  def record(thank: Thank): Future[Boolean]

  def get(user: UserID, yearMonth: YearMonth): Future[UserStat]

}
