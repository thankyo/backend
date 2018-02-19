package com.clemble.loveit.payment.service

import java.time.{LocalDateTime, YearMonth}
import javax.inject.{Inject, Singleton}

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.clemble.loveit.common.model.{Amount, UserID}
import com.clemble.loveit.common.util.LoveItCurrency
import com.clemble.loveit.payment.model.ChargeStatus.ChargeStatus
import com.clemble.loveit.payment.model.PayoutStatus.PayoutStatus
import com.clemble.loveit.payment.model.{ChargeStatus, EOMCharge, EOMPayout, EOMStatistics, EOMStatus, PayoutAccount, PayoutStatus, PendingTransaction, UserPayment}
import com.clemble.loveit.payment.service.repository._
import play.api.Logger

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
                                              transactionService: PendingTransactionService,
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
      emptyCharges <- doCreateEmptyCharges(yom)
      _ <- statusRepo.updateCreateCharges(yom, createCharges.copy(empty = emptyCharges))
      applyCharges <- doApplyCharges(yom)
      _ <- statusRepo.updateApplyCharges(yom, applyCharges)
      createPayout <- doCreatePayout(yom)
      emptyPayouts <- doCreateEmptyPayouts(yom)
      _ <- statusRepo.updateCreatePayout(yom, createPayout.copy(empty = emptyPayouts))
      applyPayout <- doApplyPayout(yom)
      _ <- statusRepo.updateApplyPayout(yom, applyPayout)

      update <- statusRepo.updateFinished(yom, LocalDateTime.now())
    } yield {
      update
    }
  }

  private def applyToAll[S, T](source: Source[S, _], process: (S) => Future[T], success: T): Future[EOMStatistics] = {
    def updateStat(stat: EOMStatistics, value: Option[T]) = {
      if (value.contains(success)) {
        stat.incSuccess()
      } else {
        stat.incFailure()
      }
    }
    source.
      mapAsync(3)(process).
      map(t => Some(t)).
      recover({
        case t: Throwable =>
          Logger.error("Failure on EOM payment", t)
          None
      }).
      runWith(Sink.fold(EOMStatistics())(updateStat))
  }

  private def doCreateCharges(yom: YearMonth): Future[EOMStatistics] = {
    def createCharge(user: UserPayment): Future[ChargeStatus] = {
      val thanks = exchangeService.toThanks(user.monthlyLimit)
      val (satisfied, _) = user.charges.splitAt(thanks.toInt)
      val amount = exchangeService.toAmountWithClientFee(satisfied.size)
      val status = user.chargeAccount match {
        case Some(_) => ChargeStatus.Pending
        case _ if (EOMChargeService.isUnderMin(amount)) => ChargeStatus.UnderMin
        case None => ChargeStatus.NoBankDetails
      }
      val charge = EOMCharge(user._id, yom, user.chargeAccount, status, amount, None, satisfied)
      chargeRepo.
        save(charge).
        map(_.status).
        recover({
          case _ => ChargeStatus.FailedToCreate
        })
    }

    applyToAll(paymentRepo.find(), createCharge, ChargeStatus.Pending)
  }

  private def doCreateEmptyCharges(yom: YearMonth): Future[Int] = {
    transactionService.findUsersWithoutCharges().map(_.size)
  }

  private def doApplyCharges(yom: YearMonth): Future[EOMStatistics] = {
    def applyCharge(charge: EOMCharge): Future[ChargeStatus] = {
      for {
        (status, details) <- chargeService.process(charge)
        _ <- chargeRepo.updatePending(charge.user, charge.yom, status, details)
      } yield {
        if (status == ChargeStatus.Success) transactionService.removeCharges(charge.user, charge.transactions)
        status
      }
    }

    applyToAll(chargeRepo.listPending(yom), applyCharge, ChargeStatus.Success)
  }

  private def doCreatePayout(yom: YearMonth): Future[EOMStatistics] = {
    def toPayoutMap(charge: EOMCharge): Map[UserID, List[PendingTransaction]] = {
      charge.transactions.groupBy(_.project.user)
    }

    def combinePayoutMaps(agg: Map[UserID, List[PendingTransaction]], payout: Map[UserID, List[PendingTransaction]]): Map[UserID, List[PendingTransaction]] = {
      agg ++ payout.map({
        case (user, transactions) =>
          user -> (transactions ++ agg.getOrElse(user, List.empty[PendingTransaction]))
      })
    }

    def toPayout(user: UserID, ptAcc: Option[PayoutAccount], payout: List[PendingTransaction]): EOMPayout = {
      val amount = exchangeService.toAmountAfterPlatformFee(payout.size, LoveItCurrency.DEFAULT)
      EOMPayout(user, yom, ptAcc, amount, PayoutStatus.Pending, payout)
    }

    def savePayouts(payoutMap: (UserID, List[PendingTransaction])): Future[Boolean] = {
      val (user, payout) = payoutMap
      for {
        ptAcc <- payoutAccService.getPayoutAccount(user)
        saved <- payoutRepo.save(toPayout(user, ptAcc, payout))
      } yield {
        saved
      }
    }

    chargeRepo.
      listSuccessful(yom).
      map(toPayoutMap).
      runWith(Sink.fold(Map.empty[UserID, List[PendingTransaction]])((agg, ch) => combinePayoutMaps(agg, ch))).
      flatMap(payoutMap => applyToAll(Source(payoutMap), savePayouts, true))
  }

  private def doCreateEmptyPayouts(yom: YearMonth): Future[Int] = {
    for {
      usersWithIncoming <- transactionService.findUsersWithPayouts()
      pendingPayouts <- payoutRepo.listCreated(yom)
      userWithNoPayouts = usersWithIncoming.filterNot(pendingPayouts.contains)
      emptyPayouts = userWithNoPayouts.map(user => EOMPayout.empty(user, yom))
      saved <- Future.sequence(emptyPayouts.map(payoutRepo.save))
    } yield {
      saved.size
    }
  }

  private def doApplyPayout(yom: YearMonth): Future[EOMStatistics] = {
    def applyPayout(payout: EOMPayout): Future[PayoutStatus] = {
      for {
        (status, details) <- payoutService.process(payout)
        _ <- payoutRepo.updatePending(payout.user, payout.yom, status, details)
      } yield {
        transactionService.removePayouts(payout.user, payout.transactions)
        status
      }
    }

    applyToAll(payoutRepo.listPending(yom), applyPayout, PayoutStatus.Success)
  }

}


