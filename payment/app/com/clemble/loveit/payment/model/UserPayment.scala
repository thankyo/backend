package com.clemble.loveit.payment.model

import com.clemble.loveit.common.model.{Amount, Money, UserID}
import com.clemble.loveit.common.util.LoveItCurrency
import com.clemble.loveit.user.model.{User, UserAware}
import play.api.libs.json._

/**
  * User view for Payments
  */
case class UserPayment(
                        _id: UserID,
                        balance: Amount = 0,
                        chargeAccount: Option[ChargeAccount] = None,
                        payoutAccount: Option[PayoutAccount] = None,
                        monthlyLimit: Money = UserPayment.DEFAULT_LIMIT,
                        pending: List[PendingTransaction] = List.empty[PendingTransaction]
                      ) extends UserAware {

  val user: UserID = _id

}

object UserPayment {

  val DEFAULT_LIMIT = Money(BigDecimal(10), LoveItCurrency.getInstance("USD"))

  implicit val jsonFormat: OFormat[UserPayment] = Json.format[UserPayment]

  def from(user: User): UserPayment = {
    UserPayment(user.id)
  }

}