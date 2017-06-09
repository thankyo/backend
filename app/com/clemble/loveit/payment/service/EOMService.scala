package com.clemble.loveit.payment.service

import java.time.YearMonth

import com.clemble.loveit.payment.model.EOMStatus

import scala.concurrent.Future

trait EOMService {

  def get(yom: YearMonth): Future[Option[EOMStatus]]

  def run(yom: YearMonth): Future[EOMStatus]

}
