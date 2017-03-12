package com.clemble.thank.social.auth

import com.clemble.thank.model.User
import com.clemble.thank.service.repository.UserRepository
import securesocial.core.{BasicProfile, PasswordInfo}
import securesocial.core.providers.MailToken
import securesocial.core.services.{SaveMode, UserService => SecureSocialUserService}
import com.google.inject.Inject

import scala.concurrent.{ExecutionContext, Future}

case class SimpleSecureSocialUserService @Inject()(
                                                    val userRep: UserRepository,
                                                    implicit val ec: ExecutionContext
) extends SecureSocialUserService[User]  {

  override def find(providerId: String, userId: String): Future[Option[BasicProfile]] = {
    userRep.findLinked(providerId, userId).map(_.flatMap(_.findBySocialProfile(providerId, userId)))
  }

  override def save(profile: BasicProfile, mode: SaveMode): Future[User] = {
    userRep.
      findLinked(profile.providerId, profile.userId).
      flatMap(userOpt => {
        val user = userOpt.map(_.link(profile)).getOrElse(User.fromProfile(profile))
        userRep.save(user)
      })
  }

  override def link(current: User, to: BasicProfile): Future[User] = {
    val updatedUser = current.link(to)
    userRep.save(updatedUser)
  }

  override def findByEmailAndProvider(email: String, providerId: String): Future[Option[BasicProfile]] = ???

  override def passwordInfoFor(user: User): Future[Option[PasswordInfo]] = ???

  override def updatePasswordInfo(user: User, info: PasswordInfo): Future[Option[BasicProfile]] = ???

  override def saveToken(token: MailToken): Future[MailToken] = ???

  override def findToken(token: String): Future[Option[MailToken]] = ???

  override def deleteToken(uuid: String): Future[Option[MailToken]] = ???

  override def deleteExpiredTokens(): Unit = ???
}
