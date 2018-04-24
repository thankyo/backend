package com.clemble.loveit.dev.service

import com.clemble.loveit.auth.model.requests.RegistrationRequest
import com.clemble.loveit.auth.service.{AuthService, UserLoggedIn, UserRegister}
import com.clemble.loveit.common.error.FieldValidationError
import com.clemble.loveit.common.model._
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.dev.service.DevStripeUtils._
import com.clemble.loveit.payment.model.ChargeAccount
import com.clemble.loveit.payment.service.ChargeAccountService
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

@Singleton
case class DevSupportersInitializer @Inject()(authService: AuthService, eventBusManager: EventBusManager, accountService: ChargeAccountService, implicit val ec: ExecutionContext) {

  def initialize(requests: Seq[RegistrationRequest]): Future[Seq[UserID]] = {
    for {
      users <- ensureUserExist(requests)
      _ <- ensureChargeAccounts(users)
    } yield {
      users
    }
  }

  private def ensureUserExist(supporterProfiles: Seq[RegistrationRequest]): Future[Seq[UserID]] = {
    val supporters = for {
      supporter <- supporterProfiles
    } yield {
      authService.register(supporter).map({
        case UserRegister(user, _) =>
          eventBusManager.publish(SignUpEvent(user, null))
          Some(user.id)
        case UserLoggedIn(user, _) =>
          eventBusManager.publish(LoginEvent(user, null))
          None
      }).recover({
        case _: FieldValidationError => None
      })
    }

    Future.sequence(supporters).map(_.flatten)
  }

  private def ensureChargeAccounts(users: Seq[UserID]): Future[List[ChargeAccount]] = {
    val usersWithCard = users
    //.filter(_ => Random.nextBoolean())
    val fCardTask = Future.sequence(
      usersWithCard.map(user => accountService.updateChargeAccount(user, someValidStripeToken()))
    )

    val usersWithoutCard = users.filterNot(usersWithCard.contains)
    val usersWithInvalidCards = usersWithoutCard.filter(_ => Random.nextBoolean())
    val fInvalidCardsTask = Future.sequence(
      usersWithInvalidCards.map(user => accountService.updateChargeAccount(user, someInValidStripeToken()))
    )

    Future.sequence(List(fCardTask, fInvalidCardsTask)).map(_.flatten)
  }

}
