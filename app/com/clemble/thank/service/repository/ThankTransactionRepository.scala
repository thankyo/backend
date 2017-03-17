package com.clemble.thank.service.repository

import com.clemble.thank.model.{ThankTransaction}

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
  def save(payment: ThankTransaction): Future[ThankTransaction]

}
