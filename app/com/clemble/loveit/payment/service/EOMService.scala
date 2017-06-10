package com.clemble.loveit.payment.service

import java.time.YearMonth
import javax.inject.{Inject, Singleton}

import com.clemble.loveit.payment.model.{EOMStatistics, EOMStatus}
import com.clemble.loveit.payment.service.repository.EOMStatusRepository
import org.joda.time.DateTime

import scala.concurrent.{ExecutionContext, Future}

/**
  * EOMService update EOM Service
  */
trait EOMService {

  /**
    * Simple wrap of repo access
    */
  def getStatus(yom: YearMonth): Future[Option[EOMStatus]]

  /**
    * Running yom for EOMStatus, if there is no EOMStatus exists
    */
  def run(yom: YearMonth): Future[EOMStatus]

}

@Singleton
case class SimpleEOMService @Inject()(repo: EOMStatusRepository, implicit val ec: ExecutionContext) extends EOMService {

  override def getStatus(yom: YearMonth): Future[Option[EOMStatus]] = {
    repo.get(yom)
  }

  override def run(yom: YearMonth): Future[EOMStatus] = {
    val status = EOMStatus(yom)
    val fSaved = repo.save(status)
    fSaved.onSuccess({ case status => doRun(yom)})
    fSaved
  }

  private def doRun(yom: YearMonth) = {
    for {
      createCharges <- doCreateCharges(yom)
      applyCharges <- doApplyCharges(yom)
      createPayout <- doCreatePayout(yom)
      applyPayout <- doApplyPayout(yom)
      update <- repo.update(yom, createCharges = createCharges, applyCharges = applyCharges, createPayout = createPayout, applyPayout = applyPayout, finished = DateTime.now())
    } yield {
      update
    }
  }

  private def doCreateCharges(yom: YearMonth): Future[EOMStatistics] = {
    Future.successful(EOMStatistics())
  }

  private def doApplyCharges(yom: YearMonth): Future[EOMStatistics] = {
    Future.successful(EOMStatistics())
  }

  private def doCreatePayout(yom: YearMonth): Future[EOMStatistics] = {
    Future.successful(EOMStatistics())
  }

  private def doApplyPayout(yom: YearMonth): Future[EOMStatistics] = {
    Future.successful(EOMStatistics())
  }

}


