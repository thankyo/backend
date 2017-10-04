package com.clemble.loveit.payment.service

import java.time.{LocalDateTime, YearMonth}
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.util.LoveItCurrency
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge, EOMPayout, EOMStatistics, EOMStatus, PayoutAccount, PayoutStatus, UserPayment}
import com.clemble.loveit.payment.service.repository._

import scala.concurrent.{ExecutionContext, Future}

/**
  * EOMService update EOM Service
  */
trait EOMPaymentService {

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
case class SimpleEOMPaymentService @Inject()(
                                       statusRepo: EOMStatusRepository,
                                       chargeRepo: EOMChargeRepository,
                                       payoutRepo: EOMPayoutRepository,
                                       chargeService: EOMChargeService,
                                       payoutService: EOMPayoutService,
                                       chargeAccService: ChargeAccountService,
                                       payoutAccService: PayoutAccountService,
                                       thankService: ThankTransactionService,
                                       paymentRepo: PaymentRepository,
                                       exchangeService: ExchangeService,
                                       implicit val ec: ExecutionContext,
                                       implicit val m: Materializer
                                     ) extends EOMPaymentService {

  override def getStatus(yom: YearMonth): Future[Option[EOMStatus]] = {
    statusRepo.get(yom)
  }

  override def run(yom: YearMonth): Future[EOMStatus] = {
    val status = EOMStatus(yom)
    val fSaved = statusRepo.save(status)
    fSaved.flatMap(_ => doRun(yom))
    fSaved
  }

  private def doRun(yom: YearMonth) = {
    for {
      createCharges <- doCreateCharges(yom)
      _ <- statusRepo.updateCreateCharges(yom, createCharges)
      applyCharges <- doApplyCharges(yom)
      _ <- statusRepo.updateApplyCharges(yom, applyCharges)
      createPayout <- doCreatePayout(yom)
      _ <- statusRepo.updateCreatePayout(yom, createPayout)
      applyPayout <- doApplyPayout(yom)
      _ <- statusRepo.updateApplyPayout(yom, applyPayout)
      update <- statusRepo.updateFinished(yom, LocalDateTime.now())
    } yield {
      update
    }
  }

  def successIfStatusIs[T](success: T)(stat: EOMStatistics, value: T): EOMStatistics = {
    if (value == success) {
      stat.incSuccess()
    } else {
      stat.incFailure()
    }
  }

  private def doCreateCharges(yom: YearMonth): Future[EOMStatistics] = {
    def createCharge(user: UserPayment): Future[ChargeStatus] = {
      val status = user.chargeAccount match {
        case Some(_) => ChargeStatus.Pending
        case None => ChargeStatus.NoBankDetails
      }
      val thanks = exchangeService.toThanks(user.monthlyLimit)
      val (satisfied, _) = user.pending.splitAt(thanks.toInt)
      val amount = exchangeService.toAmountWithClientFee(satisfied.size)
      val charge = EOMCharge(user._id, yom, user.chargeAccount, status, amount, None, satisfied)
      chargeRepo.
        save(charge).
        map(_.status).
        recover({
          case _ => ChargeStatus.FailedToCreate
        })
    }

    val updateStat: (EOMStatistics, ChargeStatus) => EOMStatistics =  successIfStatusIs(ChargeStatus.Pending)

    // TODO 2 - is a dark blood magic number it should be configured, based on system preferences
    paymentRepo.
      find().
      mapAsync(1)(createCharge).
      runWith(Sink.fold(EOMStatistics())(updateStat))
  }

  private def doApplyCharges(yom: YearMonth): Future[EOMStatistics] = {
    def applyCharge(charge: EOMCharge): Future[ChargeStatus] = {
      for {
        (status, details) <- chargeService.process(charge)
        _ <- chargeRepo.updatePending(charge.user, charge.yom, status, details)
      } yield {
        if (status == ChargeStatus.Success) thankService.removeAll(charge.transactions)
        status
      }
    }

    val updateStat: (EOMStatistics, ChargeStatus) => EOMStatistics =  successIfStatusIs(ChargeStatus.Success)

    chargeRepo.
      listPending(yom).
      mapAsync(1)(applyCharge).
      runWith(Sink.fold(EOMStatistics())(updateStat))
  }

  private def doCreatePayout(yom: YearMonth): Future[EOMStatistics] = {
    def toPayoutMap(charge: EOMCharge): Map[UserID, Int] = {
      charge.transactions.groupBy(_.destination).mapValues(_.size)
    }

    def combinePayoutMaps(agg: Map[UserID, Int], payout: Map[UserID, Int]): Map[UserID, Int] = {
      agg ++ payout.map({ case (user, amount) => user -> (amount + agg.getOrElse(user, 0)) })
    }

    def toPayout(user: UserID, ptAcc: Option[PayoutAccount], payout: Amount): EOMPayout = {
      val amount = exchangeService.toAmountAfterPlatformFee(payout, LoveItCurrency.DEFAULT)
      EOMPayout(user, yom, ptAcc, amount, PayoutStatus.Pending)
    }

    def savePayouts(payoutMap: (UserID, Int)): Future[Boolean] = {
      val (user, payout) = payoutMap
      for {
        ptAcc <- payoutAccService.getPayoutAccount(user)
        saved <- payoutRepo.save(toPayout(user, ptAcc, payout))
      } yield {
        saved
      }
    }

    val updateStat: (EOMStatistics, Boolean) => EOMStatistics = successIfStatusIs(true)

    chargeRepo.
      listSuccessful(yom).
      map(toPayoutMap).
      runWith(Sink.fold(Map.empty[UserID, Int])((agg, ch) => combinePayoutMaps(agg, ch))).
      flatMap(payoutMap => {
        Source(payoutMap).
          mapAsync(1)(savePayouts).
          runWith(Sink.fold(EOMStatistics())(updateStat))
      })
  }

  private def doApplyPayout(yom: YearMonth): Future[EOMStatistics] = {
    def applyPayout(payout: EOMPayout): Future[PayoutStatus] = {
      for {
        (status, details) <- payoutService.process(payout)
        _ <- payoutRepo.updatePending(payout.user, payout.yom, status, details)
      } yield {
        status
      }
    }

    val updateStat: (EOMStatistics, PayoutStatus) => EOMStatistics =  successIfStatusIs(PayoutStatus.Success)

    payoutRepo.
      listPending(yom).
      mapAsync(1)(applyPayout).
      runWith(Sink.fold(EOMStatistics())(updateStat))
  }

}


