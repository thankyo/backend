package com.clemble.loveit.payment.model

import java.util.Currency

import com.clemble.loveit.common.util.{LoveItCurrency, WriteableUtils}
import play.api.libs.json._

case class Money(
                  amount: BigDecimal,
                  currency: Currency
                ) extends Ordered[Money] {

  def this(amount: BigDecimal, currency: String) = this(amount, LoveItCurrency.getInstance(currency))

  def isNegative: Boolean = amount < 0

  def +(that: Money) = {
    require(currency == that.currency)
    Money(amount + that.amount, currency)
  }

  override def compare(that: Money): Int = {
    require(currency == that.currency)
    amount.compare(that.amount)
  }

}

object Money {

  implicit val currencyFormat = new Format[Currency] {

    override def writes(o: Currency): JsValue = JsString(o.getCurrencyCode)

    override def reads(json: JsValue): JsResult[Currency] = json match {
      case JsString(currencyCode) => JsSuccess(LoveItCurrency.getInstance(currencyCode))
      case _ => JsError(s"Can't read currency ${json}")
    }
  }

  implicit val jsonFormat = Json.format[Money]

  implicit val writeableJson = WriteableUtils.jsonToWriteable[Money]

}