package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.util.AuthEnv
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.{PaymentRepository, UserPaymentRepository}
import com.clemble.loveit.user.model.User
import com.mohiva.play.silhouette.api.{Environment, SignUpEvent}

import scala.concurrent.Future

trait UserPaymentService {

  def createAndSave(user: User): Future[Boolean]

}

case class UserPaymentSignUpListener @Inject()(uPayS: UserPaymentService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user : User, _) =>
      uPayS.createAndSave(user)
  }

}

@Singleton
class SimpleUserPaymentService @Inject()(actorSystem: ActorSystem, env: Environment[AuthEnv], paymentRepository: PaymentRepository) extends UserPaymentService {

  {
    val subscriber = actorSystem.actorOf(Props(UserPaymentSignUpListener(this)))
    env.eventBus.subscribe(subscriber, classOf[SignUpEvent[User]])
  }

  override def createAndSave(user: User) = {
    val payment = UserPayment from user
    paymentRepository.save(payment)
  }
}
