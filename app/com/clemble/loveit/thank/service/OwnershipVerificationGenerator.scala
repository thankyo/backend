package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, Pending, ResourceOwnership}
import com.mohiva.play.silhouette.api.crypto.Crypter

trait OwnershipVerificationGenerator {

  def generate(user: UserID, ownership: ResourceOwnership): OwnershipVerificationRequest

}

@Singleton
case class CryptOwnershipVerificationGenerator @Inject()(crypter: Crypter) extends OwnershipVerificationGenerator {

  override def generate(user: UserID, ownership: ResourceOwnership): OwnershipVerificationRequest = {
    val verificationCode = crypter.encrypt(s"${user}@${ownership.resource.uri}")
    OwnershipVerificationRequest(
      status = Pending,
      resource = ownership.resource,
      ownershipType = ownership.ownershipType,
      requester = user,
      verificationCode
    )
  }

}
