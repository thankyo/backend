package com.clemble.loveit.payment.service

import java.util.Currency

import com.clemble.loveit.common.model.Amount
import com.clemble.loveit.payment.model.Money
import com.google.inject.Singleton

trait ExchangeService {

  def toAmount(currency: Currency): Amount

  def toAmount(amount: Amount, currency: Currency): Money = {
    val perCurUnit = toAmount(currency)
    val amountMoney = amount / perCurUnit
    Money(amountMoney, currency)
  }

  def toThanks(money: Money): Amount = {
    (money.amount * toAmount(money.currency)).toLongExact
  }

}

@Singleton
case class InMemoryExchangeService(currencyToAmount: Map[Currency, Amount]) extends ExchangeService {
  override def toAmount(currency: Currency): Amount = {
    currencyToAmount(currency)
  }
}