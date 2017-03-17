package com.clemble.thank.service

import java.util.Currency

import com.clemble.thank.model.{Amount, Money}

trait ExchangeService {

  def toAmount(currency: Currency): Amount

  def toThanks(money: Money): Amount = {
    (money.amount * toAmount(money.currency)).toLongExact
  }

}

case class InMemoryExchangeService(currencyToAmount: Map[Currency, Amount]) extends ExchangeService {
  override def toAmount(currency: Currency): Amount = currencyToAmount(currency)
}