package com.clemble.loveit.thank.service

import java.time.YearMonth
import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.{ThankEvent, UserID}
import com.clemble.loveit.thank.model.UserStat
import com.clemble.loveit.thank.service.repository.UserStatRepo

import scala.concurrent.Future

trait UserStatService {

  def get(user: UserID, yearMonth: YearMonth): Future[UserStat]

  def record(thank: ThankEvent): Future[Boolean]

}


case class UserStatThankListener(service: UserStatService) extends Actor {
  override def receive = {
    case thank: ThankEvent =>
      service.record(thank)
  }
}


@Singleton
class SimpleUserStatService @Inject()(
                                       repo: UserStatRepo,
                                       actorSystem: ActorSystem,
                                       thankEventBus: ThankEventBus
                                     ) extends UserStatService {

  {
    val subscriber = actorSystem.actorOf(Props(UserStatThankListener(this)))
    thankEventBus.subscribe(subscriber, classOf[ThankEvent])
  }

  override def get(user: UserID, yearMonth: YearMonth) = {
    repo.get(user, yearMonth)
  }

  override def record(thank: ThankEvent) = {
    repo.increase(thank.project.user)
  }

}