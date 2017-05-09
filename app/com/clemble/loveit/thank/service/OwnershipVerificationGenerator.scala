package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.common.util.IDGenerator
import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, Pending, ResourceOwnership}
import com.mohiva.play.silhouette.api.crypto.Crypter

trait OwnershipVerificationGenerator {

  def generate(user: UserID, ownership: ResourceOwnership): OwnershipVerificationRequest[Resource]

}

@Singleton
case class CryptOwnershipVerificationGenerator @Inject()(crypter: Crypter) extends OwnershipVerificationGenerator {

  override def generate(user: UserID, ownership: ResourceOwnership): OwnershipVerificationRequest[Resource] = {
    val verificationCode = crypter.encrypt(s"${user}@${ownership.resource.uri}")
    OwnershipVerificationRequest(
      id = IDGenerator.generate(),
      status = Pending,
      resource = ownership.resource,
      ownershipType = ownership.ownershipType,
      requester = user,
      verificationCode
    )
  }

}
