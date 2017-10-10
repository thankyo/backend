package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.{RepositorySpec, ThankExecutor, ThankScenario, ThankSpecification}
import com.clemble.loveit.common.model.{Money, UserID}
import com.clemble.loveit.payment.model.UserPayment

class PaymentLimitRepositorySpec extends RepositorySpec
  with PaymentLimitScenario
  with PaymentLimitExecutor {

  val limitRepo = dependency[PaymentLimitRepository]

  def getMonthlyLimit(user: UserID): Option[Money] = await(limitRepo.getMonthlyLimit(user))
  def setMonthlyLimit(user: UserID, limit: Money): Boolean = await(limitRepo.setMonthlyLimit(user, limit))

  "Update Limit" in {
    val user = createUser()
    val limitBefore = getMonthlyLimit(user)
    getMonthlyLimit(user) shouldEqual Some(UserPayment.DEFAULT_LIMIT)

    val newLimit = someRandom[Money]
    setMonthlyLimit(user, newLimit) shouldEqual true

    val limitAfter = getMonthlyLimit(user)

    limitAfter shouldEqual Some(newLimit)
    limitBefore shouldNotEqual limitAfter
  }

}

trait PaymentLimitExecutor extends ThankExecutor {

  def getMonthlyLimit(user: UserID): Option[Money]
  def setMonthlyLimit(user: UserID, limit: Money): Boolean

}

trait PaymentLimitScenario extends PaymentLimitExecutor with ThankScenario {

  "Update Limit" in {
    val user = createUser()
    val limitBefore = getMonthlyLimit(user)
    getMonthlyLimit(user) shouldEqual Some(UserPayment.DEFAULT_LIMIT)

    val newLimit = someRandom[Money]
    setMonthlyLimit(user, newLimit) shouldEqual true

    val limitAfter = getMonthlyLimit(user)

    limitAfter shouldEqual Some(newLimit)
    limitBefore shouldNotEqual limitAfter
  }

}