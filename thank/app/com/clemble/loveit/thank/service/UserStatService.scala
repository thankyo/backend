package com.clemble.loveit.thank.service

import java.time.YearMonth

import com.clemble.loveit.common.model.{ThankTransaction, UserID}
import com.clemble.loveit.thank.model.UserStat
import com.clemble.loveit.thank.service.repository.UserStatRepo

import scala.concurrent.Future

trait UserStatService {

  def get(user: UserID, yearMonth: YearMonth): Future[UserStat]

  def record(thank: ThankTransaction): Future[Boolean]

}



class SimpleUserStatService(repo: UserStatRepo) extends UserStatService {

  override def get(user: UserID, yearMonth: YearMonth) = repo.get(user, yearMonth)

  override def record(thank: ThankTransaction) = {
    ???
  }

}