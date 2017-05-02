package com.clemble.loveit.common.util

import java.util.Currency

object LoveItCurrency {

  //TODO this is to be removed in future releases
  val DEFAULT = Currency.getInstance("USD")

  def getOrDefault(currencyOpt: Option[String]): Currency = {
    currencyOpt.map(Currency.getInstance).getOrElse(DEFAULT)
  }

  def getInstance(currencyCode: String): Currency = Currency.getInstance(currencyCode)

}
