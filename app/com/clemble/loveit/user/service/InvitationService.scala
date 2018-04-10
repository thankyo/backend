package com.clemble.loveit.user.service

import javax.inject.Inject

import com.clemble.loveit.user.model.Invitation
import com.clemble.loveit.user.service.repository.InvitationRepository

import scala.concurrent.Future

trait InvitationService {

  def save(inv: Invitation): Future[Invitation]

}

case class SimpleInvitationService @Inject()(repo: InvitationRepository) extends InvitationService {

  override def save(inv: Invitation): Future[Invitation] = {
    repo.save(inv)
  }

}
