package com.clemble.loveit.payment.service

import java.time.{LocalDateTime, YearMonth}
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.util.LoveItCurrency
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge, EOMPayout, EOMStatistics, EOMStatus, PayoutAccount, PayoutStatus, UserPayment}
import com.clemble.loveit.payment.service.repository._

import scala.collection.immutable
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
                                       payoutRepo: EOMPayoutRepository,
                                       chargeService: EOMChargeService,
                                       payoutService: EOMPayoutService,
                                       paymentAccService: PaymentAccountService,
                                       thankService: ThankTransactionService,
                                       paymentRepo: PaymentRepository,
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
    fSaved.onSuccess({ case _ => doRun(yom) })
    fSaved
  }

  private def doRun(yom: YearMonth) = {
    for {
      createCharges <- doCreateCharges(yom)
      applyCharges <- doApplyCharges(yom)
      createPayout <- doCreatePayout(yom)
      applyPayout <- doApplyPayout(yom)
      update <- statusRepo.update(yom, createCharges = createCharges, applyCharges = applyCharges, createPayout = createPayout, applyPayout = applyPayout, finished = LocalDateTime.now())
    } yield {
      update
    }
  }

  private def doCreateCharges(yom: YearMonth): Future[EOMStatistics] = {
    def updateStatistics(stat: EOMStatistics, res: EOMCharge): EOMStatistics = {
      res.status match {
        case ChargeStatus.Pending => stat.incSuccess()
        case _ => stat.incFailure()
      }
    }

    def toCharge(user: UserPayment): EOMCharge = {
      val status = user.chargeAccount match {
        case Some(_) => ChargeStatus.Pending
        case None => ChargeStatus.NoBankDetails
      }
      val thanks = exchangeService.toThanks(user.monthlyLimit)
      val (satisfied, _) = user.pending.splitAt(thanks.toInt)
      val amount = exchangeService.toAmountWithClientFee(satisfied.size)
      EOMCharge(user._id, yom, user.chargeAccount, status, amount, None, satisfied)
    }

    def createCharge(user: UserPayment): Future[EOMCharge] = {
      val charge = toCharge(user)
      chargeRepo.save(charge)
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
        case ChargeStatus.Success => stat.incSuccess()
        case _ => stat.incFailure()
      }
    }

    def applyCharge(charge: EOMCharge): Future[ChargeStatus] = {
      for {
        (status, details) <- chargeService.process(charge)
        _ <- chargeRepo.updatePending(charge.user, charge.yom, status, details)
      } yield {
        if (status == ChargeStatus.Success) thankService.removeAll(charge.transactions)
        status
      }
    }

    chargeRepo.
      listPending(yom).
      mapAsync(1)(applyCharge).
      runWith(Sink.fold(EOMStatistics())((stat, res) => updateStatistics(stat, res)))
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

    def savePayouts(payoutMap: Map[UserID, Int]): Future[immutable.Iterable[Boolean]] = {
      val fSavedPayouts = for {
        (user, payout) <- payoutMap
      } yield {
        for {
          ptAcc <- paymentAccService.getPayoutAccount(user)
          saved <- payoutRepo.save(toPayout(user, ptAcc, payout))
        } yield {
          saved
        }
      }
      Future.sequence(fSavedPayouts)
    }

    def updateStatus(stat: EOMStatistics, payouts: Iterable[Boolean]) = {
      payouts.foldLeft(stat)((stat, success) => {
        if (success) stat.incSuccess() else stat.incFailure()
      })
    }

    chargeRepo.
      listSuccessful(yom).
      map(toPayoutMap).
      runWith(Sink.fold(Map.empty[UserID, Int])((agg, ch) => combinePayoutMaps(agg, ch))).
      flatMap(payoutMap => savePayouts(payoutMap)).map(updateStatus(EOMStatistics(), _))
  }

  private def doApplyPayout(yom: YearMonth): Future[EOMStatistics] = {
    def updateStatistics(stat: EOMStatistics, status: PayoutStatus): EOMStatistics = {
      status match {
        case PayoutStatus.Success => stat.incSuccess()
        case _ => stat.incFailure()
      }
    }

    def applyPayout(payout: EOMPayout): Future[PayoutStatus] = {
      for {
        (status, details) <- payoutService.process(payout)
        _ <- payoutRepo.updatePending(payout.user, payout.yom, status, details)
      } yield {
        status
      }
    }

    payoutRepo.
      listPending(yom).
      mapAsync(1)(applyPayout).
      runWith(Sink.fold(EOMStatistics())((stat, res) => updateStatistics(stat, res)))
  }

}


