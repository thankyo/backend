package com.clemble.loveit.user.service.repository

import com.clemble.loveit.user.model.Invitation

import scala.concurrent.Future

trait InvitationRepository {

  def save(invite: Invitation): Future[Invitation]

}
