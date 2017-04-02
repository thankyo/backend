package com.clemble.loveit.util

import java.util.Currency

object LoveItCurrency {

  val DEFAULT = Currency.getInstance("USD")

  def getOrDefault(currencyOpt: Option[String]): Currency = {
    currencyOpt.map(Currency.getInstance).getOrElse(DEFAULT)
  }

  def getInstance(currencyCode: String): Currency = Currency.getInstance(currencyCode)

}
