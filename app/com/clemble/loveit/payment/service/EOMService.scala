package com.clemble.loveit.payment.service

import java.time.YearMonth
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge, EOMStatistics, EOMStatus, UserPayment}
import com.clemble.loveit.payment.service.repository.{EOMChargeRepository, EOMStatusRepository, UserPaymentRepository}
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
case class SimpleEOMService @Inject()(
                                       statusRepo: EOMStatusRepository,
                                       chargeRepo: EOMChargeRepository,
                                       chargeService: EOMChargeService,
                                       paymentRepo: UserPaymentRepository,
                                       exchangeService: ExchangeService,
                                       implicit val ec: ExecutionContext,
                                       implicit val m: Materializer
                                     ) extends EOMService {

  override def getStatus(yom: YearMonth): Future[Option[EOMStatus]] = {
    statusRepo.get(yom)
  }

  override def run(yom: YearMonth): Future[EOMStatus] = {
    val status = EOMStatus(yom)
    val fSaved = statusRepo.save(status)
    fSaved.onSuccess({ case _ => doRun(yom)})
    fSaved
  }

  private def doRun(yom: YearMonth) = {
    for {
      createCharges <- doCreateCharges(yom)
      applyCharges <- doApplyCharges(yom)
      createPayout <- doCreatePayout(yom)
      applyPayout <- doApplyPayout(yom)
      update <- statusRepo.update(yom, createCharges = createCharges, applyCharges = applyCharges, createPayout = createPayout, applyPayout = applyPayout, finished = DateTime.now())
    } yield {
      update
    }
  }

  private def doCreateCharges(yom: YearMonth): Future[EOMStatistics] = {
    def toCharge(user: UserPayment): Option[EOMCharge] = {
      user.
        bankDetails.
        map(bd => {
          val thanks = exchangeService.toThanks(user.monthlyLimit)
          val (satisfied, unsatisfied) = user.pending.splitAt(thanks.toInt)
          val charge = exchangeService.toAmount(satisfied.size)
          val amount = charge + bd.fee
          EOMCharge(user.id, yom, bd, ChargeStatus.Pending, amount, None, satisfied, unsatisfied)
        })
    }

    def createCharge(user: UserPayment): Future[Option[EOMCharge]] = {
      toCharge(user) match {
        case Some(charge) => chargeRepo.save(charge).map(Some(_))
        case None => Future.successful(None)
      }
    }

    def updateStatistics(stat: EOMStatistics, res: Option[EOMCharge]): EOMStatistics = {
      res match {
        case Some(_) => stat.copy(success = stat.success + 1, total = stat.total + 1)
        case _ => stat.copy(failed = stat.failed + 1, total = stat.total + 1)
      }
    }

    // TODO 2 - is a dark blood magic number it should be configured, based on system preferences
    paymentRepo.
      find().
      mapAsync(1)(createCharge).
      runWith(Sink.fold(EOMStatistics())((stat, res) => updateStatistics(stat, res)))
  }

  private def doApplyCharges(yom: YearMonth): Future[EOMStatistics] = {
    def updateStatistics(stat: EOMStatistics, status: ChargeStatus): EOMStatistics = {
      status match {
        case ChargeStatus.Success => stat.copy(success = stat.success + 1, total = stat.total + 1)
        case _ => stat.copy(failed = stat.failed + 1, total = stat.failed + 1)
      }
    }

    def applyCharge(charge: EOMCharge): Future[ChargeStatus] = {
      for {
        (status, details) <- chargeService.process(charge)
        _ <- chargeRepo.updatePending(charge.user, charge.yom, status, details)
      } yield {
        status
      }
    }

    chargeRepo.
      listPending(yom).
      mapAsync(1)(applyCharge).
      runWith(Sink.fold(EOMStatistics())((stat, res) => updateStatistics(stat, res)))
  }

  private def doCreatePayout(yom: YearMonth): Future[EOMStatistics] = {
    Future.successful(EOMStatistics())
  }

  private def doApplyPayout(yom: YearMonth): Future[EOMStatistics] = {
    Future.successful(EOMStatistics())
  }

}


