package com.clemble.loveit.payment.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json._

/**
  * Stripe account credentials
  */
case class PayoutAccount(accountId: String, refreshToken: String, accessToken: String) extends PaymentAccount

object PayoutAccount {

  /**
    * JSON format for [[PayoutAccount]]
    */
  implicit val jsonFormat: OFormat[PayoutAccount] = Json.format[PayoutAccount]
  implicit val chargeAccountWriteable: Writeable[PayoutAccount] = WriteableUtils.jsonToWriteable[PayoutAccount]

}
