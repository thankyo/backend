package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.{Money, UserPayment}

class PaymentLimitRepositorySpec extends RepositorySpec {

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