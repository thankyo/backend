package com.clemble.loveit.payment.service.repository

import com.clemble.loveit.payment.model.ThankTransaction
import com.clemble.loveit.user.service.repository.UserAwareRepository

import scala.concurrent.Future

/**
  * [[ThankTransaction]] repository
  */
trait ThankTransactionRepository extends UserAwareRepository[ThankTransaction] {

  /**
    * Saves payment for future reference
    *
    * @param payment payment to save
    * @return saved Payment presentation
    */
  def save(payment: ThankTransaction): Future[Boolean]

  /**
    * Removes all specified thanks
    */
  def removeAll(thanks: Seq[ThankTransaction]): Future[Boolean]

}
