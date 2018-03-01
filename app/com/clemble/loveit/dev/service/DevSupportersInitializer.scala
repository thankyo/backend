package com.clemble.loveit.dev.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.payment.service.ChargeAccountService
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}

import scala.concurrent.{ExecutionContext, Future}
import DevStripeUtils._
import scala.util.Random

@Singleton
case class DevSupportersInitializer @Inject()(authService: AuthService, eventBusManager: EventBusManager, accountService: ChargeAccountService, implicit val ec: ExecutionContext)  {

  def initialize(requests: Seq[RegistrationRequest]): Future[Seq[UserID]] = {
    ensureUserExist(requests)
  }

  private def ensureUserExist(creators: Seq[RegistrationRequest]): Future[Seq[UserID]] = {
    val supporters = for {
      creator <- creators
    } yield {
      authService.register(creator).map({
        case UserRegister(user, _) =>
          eventBusManager.publish(SignUpEvent(user, null))
          Some(user.id)
        case UserLoggedIn(user, _) =>
          eventBusManager.publish(LoginEvent(user, null))
          None
      })
    }
    val fUsers = Future.sequence(supporters).map(_.flatten)

    fUsers.flatMap(users => {
      val usersWithCard = users//.filter(_ => Random.nextBoolean())
      val fCardTask = Future.sequence(
        usersWithCard.map(user => accountService.updateChargeAccount(user, someValidStripeToken()))
      )

      val usersWithoutCard = users.filterNot(usersWithCard.contains)
      val usersWithInvalidCards = usersWithoutCard.filter(_ => Random.nextBoolean())
      val fInvalidCardsTask = Future.sequence(
        usersWithInvalidCards.map(user => accountService.updateChargeAccount(user, someValidStripeToken()).recover({ case _ => true}))
      )

      Future.sequence(List(fCardTask, fInvalidCardsTask)).map(_ => users)
    })
  }

}
