package com.clemble.loveit.payment.service.repository

import java.time.YearMonth

import com.clemble.loveit.payment.model.EOMStatus

import scala.concurrent.Future

trait EOMStatusRepository {

  def get(yom: YearMonth): Future[Option[YearMonth]]

  def save(status: EOMStatus): Future[EOMStatus]

}
