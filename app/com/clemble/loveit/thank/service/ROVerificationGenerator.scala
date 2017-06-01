package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{ROVerificationRequest, Pending}
import com.mohiva.play.silhouette.api.crypto.Crypter

trait ROVerificationGenerator {

  def generate(user: UserID, ownership: Resource): ROVerificationRequest[Resource]

}

@Singleton
case class CryptROVerificationGenerator @Inject()(crypter: Crypter) extends ROVerificationGenerator {

  override def generate(user: UserID, resource: Resource): ROVerificationRequest[Resource] = {
    val verificationCode = crypter.encrypt(s"${user}@${resource.uri}")
    ROVerificationRequest(
      id = IDGenerator.generate(),
      status = Pending,
      resource = resource,
      requester = user,
      verificationCode
    )
  }

}
