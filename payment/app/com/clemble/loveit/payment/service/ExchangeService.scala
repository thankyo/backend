package com.clemble.loveit.payment.service

import java.util.Currency

import com.clemble.loveit.common.model.{Amount, Money}
import javax.inject.Singleton

import com.clemble.loveit.common.util.LoveItCurrency

trait ExchangeService {

  def toAmount(currency: Currency): Amount

  def toAmount(amount: Amount): Money = toAmount(amount, LoveItCurrency.DEFAULT)

  def toAmountWithClientFee(amount: Amount): Money = {
    toAmount(amount, LoveItCurrency.DEFAULT) + ExchangeService.CLIENT_FEE
  }

  def toAmount(amount: Amount, currency: Currency): Money = {
    val perCurUnit = toAmount(currency)
    val amountMoney = amount / perCurUnit
    Money(amountMoney, currency)
  }

  def toAmountAfterPlatformFee(amount: Amount, currency: Currency): Money = {
    val perCurUnit = toAmount(currency)
    val amountBefore = BigDecimal(amount / perCurUnit)
    val amountAfter = amountBefore * 0.9
    Money(amountAfter, currency)
  }

  def toThanks(money: Money): Amount = {
    (money.amount * toAmount(money.currency)).toLongExact
  }

}

object ExchangeService {

  private val CLIENT_FEE = Money(0.3, LoveItCurrency.getInstance("USD"))

}

@Singleton
case class InMemoryExchangeService(currencyToAmount: Map[Currency, Amount]) extends ExchangeService {
  override def toAmount(currency: Currency): Amount = {
    currencyToAmount(currency)
  }
}