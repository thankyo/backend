package com.clemble.loveit.payment.service

import javax.inject.{Inject, Singleton}

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.util.{AuthEnv, EventBusManager}
import com.clemble.loveit.payment.model.UserPayment
import com.clemble.loveit.payment.service.repository.{PaymentRepository, UserPaymentRepository}
import com.clemble.loveit.common.model.User
import com.mohiva.play.silhouette.api.{Environment, SignUpEvent}

import scala.concurrent.Future

trait UserPaymentService {

  def create(user: User): Future[Boolean]

}

case class UserPaymentSignUpListener @Inject()(uPayS: UserPaymentService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user : User, _) =>
      uPayS.create(user)
  }

}

@Singleton
class SimpleUserPaymentService @Inject()(eventBusManager: EventBusManager, paymentRepository: PaymentRepository) extends UserPaymentService {

  eventBusManager.onSignUp(Props(UserPaymentSignUpListener(this)))

  override def create(user: User) = {
    val payment = UserPayment from user
    paymentRepository.save(payment)
  }
}
