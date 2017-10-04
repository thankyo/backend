package com.clemble.loveit.payment.service.repository

import java.time.{LocalDateTime, YearMonth}

import com.clemble.loveit.payment.model.{EOMStatistics, EOMStatus}

import scala.concurrent.Future

trait EOMStatusRepository {

  def get(yom: YearMonth): Future[Option[EOMStatus]]

  def save(status: EOMStatus): Future[EOMStatus]

  def updateCreateCharges(yom: YearMonth, createCharges: EOMStatistics): Future[Boolean]

  def updateApplyCharges(yom: YearMonth, applyCharges: EOMStatistics): Future[Boolean]

  def updateCreatePayout(yom: YearMonth, createPayout: EOMStatistics): Future[Boolean]

  def updateApplyPayout(yom: YearMonth, applyPayout: EOMStatistics): Future[Boolean]

  def updateFinished(yom: YearMonth, finished: LocalDateTime): Future[Boolean]

}
