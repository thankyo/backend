package com.clemble.loveit.payment.model

import java.util.Currency

import com.braintreegateway.{Transaction => BraintreeTransaction}
import com.clemble.loveit.common.util.LoveItCurrency
import com.paypal.api.payments
import play.api.libs.json._

case class Money(
                  amount: BigDecimal,
                  currency: Currency
                ) {

  def toPayPalCurrency(): payments.Currency = {
    new payments.Currency(currency.getCurrencyCode, amount.toString())
  }

}

object Money {

  implicit val currencyFormat = new Format[Currency] {

    override def writes(o: Currency): JsValue = JsString(o.getCurrencyCode())

    override def reads(json: JsValue): JsResult[Currency] = json match {
      case JsString(currencyCode) => JsSuccess(LoveItCurrency.getInstance(currencyCode))
      case _ => JsError(s"Can't read currency ${json}")
    }
  }

  implicit val jsonFormat = Json.format[Money]

  def from(transaction: BraintreeTransaction): Money = {
    Money(transaction.getAmount(), LoveItCurrency.getInstance(transaction.getCurrencyIsoCode))
  }

}