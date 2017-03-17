package com.clemble.thank.model

import java.util.Currency

import com.braintreegateway.{Transaction => BraintreeTransaction}
import play.api.libs.json._

case class Money(
                  amount: BigDecimal,
                  currency: Currency
                )

object Money {

  implicit val currencyFormat = new Format[Currency] {

    override def writes(o: Currency): JsValue = JsString(o.getCurrencyCode())

    override def reads(json: JsValue): JsResult[Currency] = json match {
      case JsString(currencyCode) => JsSuccess(Currency.getInstance(currencyCode))
      case _ => JsError(s"Can't read currency ${json}")
    }
  }

  implicit val jsonFormat = Json.format[Money]

  def from(transaction: BraintreeTransaction): Money = {
    Money(transaction.getAmount(), Currency.getInstance(transaction.getCurrencyIsoCode))
  }

}