package com.clemble.loveit.payment.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json._

/**
  * Bank details abstraction
  */
case class ChargeAccount(
                          customer: String,
                          brand: Option[String] = None,
                          last4: Option[String] = None
                        ) extends PaymentAccount {
  require(customer != null && customer.length != 0)
}

object ChargeAccount {

  /**
    * JSON format for [[ChargeAccount]]
    */
  implicit val jsonFormat: OFormat[ChargeAccount] = Json.format[ChargeAccount]
  implicit val chargeAccountWriteable: Writeable[ChargeAccount] = WriteableUtils.jsonToWriteable[ChargeAccount]

}