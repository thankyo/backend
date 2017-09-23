package com.clemble.loveit.thank.service

import javax.inject.{Inject, Named, Singleton}

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{Pending, ROVerification}
import com.mohiva.play.silhouette.api.crypto.Crypter

trait ROVerificationGenerator {

  def generate(user: UserID, res: Resource): ROVerification[Resource]

  def encrypt(user: UserID, resource: Resource): String

  def decrypt(token: String): (UserID, Resource)

}

@Singleton
case class CryptROVerificationGenerator @Inject()(@Named("rovCrypter") crypter: Crypter) extends ROVerificationGenerator {

  override def generate(user: UserID, res: Resource): ROVerification[Resource] = {
    val verificationCode = crypter.encrypt(s"${user}@${res.uri}")
    ROVerification(
      status = Pending,
      resource = res,
      verificationCode
    )
  }

  override def encrypt(user: UserID, resource: Resource): String = {
    crypter.encrypt(s"${user}@${resource.stringify()}")
  }

  override def decrypt(token: String): (UserID, Resource) = {
    val arr = crypter.decrypt(token).split("@")
    arr(0) -> Resource.from(arr(1))
  }

}
