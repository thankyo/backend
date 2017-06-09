package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.payment.model.EOMStatus

import scala.concurrent.Future

trait EOMStatusRepository {

  /**
    * @return optional EOMStatus
    */
  def get(yom: YearMonth): Future[Option[EOMStatus]]

  /**
    * Saves EOM status, if there were no previous EOMStatus created
    */
  def save(status: EOMStatus): Future[EOMStatus]

}
