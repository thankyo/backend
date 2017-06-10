package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.payment.model.EOMPayout

import scala.concurrent.Future

trait EOMPayoutRepository {

  def save(payout: EOMPayout): Future[Boolean]

}
