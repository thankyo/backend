package com.clemble.loveit.thank.service

import javax.inject.{Inject, Singleton}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.{OwnershipVerificationRequest, Pending, ResourceOwnership}
import play.api.libs.Crypto

trait OwnershipVerificationGenerator {

  def generate(user: UserID, ownership: ResourceOwnership): OwnershipVerificationRequest

}

@Singleton
case class CryptOwnershipVerificationGenerator @Inject()(encrypt: Crypto) extends OwnershipVerificationGenerator {

  override def generate(user: UserID, ownership: ResourceOwnership): OwnershipVerificationRequest = {
    val verificationCode = Crypto.encryptAES(s"${user}@${ownership.resource.uri}")
    OwnershipVerificationRequest(
      status = Pending,
      resource = ownership.resource,
      ownershipType = ownership.ownershipType,
      requester = user,
      verificationCode
    )
  }

}
