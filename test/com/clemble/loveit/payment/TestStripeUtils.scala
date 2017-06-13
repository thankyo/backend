package com.clemble.loveit.payment

import com.clemble.loveit.payment.model.StripeCustomerToken

import scala.util.Random

trait TestStripeUtils {

  private val VALID_USA_TOKENS = List(
    "tok_visa",
    "tok_visa_debit",
    "tok_mastercard",
    "tok_mastercard_debit",
    "tok_mastercard_prepaid",
    "tok_amex",
    "tok_discover",
    "tok_diners",
    "tok_jcb"
  )

  private val INVALID_TOKENS = List(
    "tok_bypassPending",
    "tok_domesticPricing",
    "tok_avsFail",
    "tok_avsLine1Fail",
    "tok_avsZipFail",
    "tok_avsUnchecked",
    "tok_cvcCheckFail",
    "tok_chargeCustomerFail",
    "tok_riskLevelElevated",
    "tok_chargeDeclined",
    "tok_chargeDeclinedFraudulent",
    "tok_chargeDeclinedIncorrectCvc",
    "tok_chargeDeclinedExpiredCard",
    "tok_chargeDeclinedProcessingError"
  )

  def someValidStripeToken(): StripeCustomerToken = VALID_USA_TOKENS(Random.nextInt(VALID_USA_TOKENS.size))
  def someInValidStripeToken(): StripeCustomerToken = INVALID_TOKENS(Random.nextInt(INVALID_TOKENS.size))

  def allValidStripeTokens: Seq[StripeCustomerToken] = VALID_USA_TOKENS
  def allInvalidTokens: Seq[StripeCustomerToken] = INVALID_TOKENS

}
