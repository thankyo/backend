package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{ROVerificationRequest, Pending, ResourceOwnership}
import com.mohiva.play.silhouette.api.crypto.Crypter

trait ROVerificationGenerator {

  def generate(user: UserID, ownership: ResourceOwnership): ROVerificationRequest[Resource]

}

@Singleton
case class CryptROVerificationGenerator @Inject()(crypter: Crypter) extends ROVerificationGenerator {

  override def generate(user: UserID, ownership: ResourceOwnership): ROVerificationRequest[Resource] = {
    val verificationCode = crypter.encrypt(s"${user}@${ownership.resource.uri}")
    ROVerificationRequest(
      id = IDGenerator.generate(),
      status = Pending,
      resource = ownership.resource,
      ownershipType = ownership.ownershipType,
      requester = user,
      verificationCode
    )
  }

}
