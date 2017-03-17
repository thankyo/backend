package com.clemble.thank.service.repository

import akka.stream.scaladsl.Source
import com.clemble.thank.model.{ThankTransaction, UserID}

import scala.concurrent.Future

/**
  * [[ThankTransaction]] repository
  */
trait ThankTransactionRepository {

  /**
    * Find all payments done by the customer
    *
    * @param user user identifier
    * @return all payments done by the user
    */
  def findByUser(user: UserID): Source[ThankTransaction, _]

  /**
    * Saves payment for future reference
    *
    * @param payment payment to save
    * @return saved Payment presentation
    */
  def save(payment: ThankTransaction): Future[ThankTransaction]

}
