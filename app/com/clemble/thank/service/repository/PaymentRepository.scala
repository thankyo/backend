package com.clemble.thank.service.repository

import com.clemble.thank.model.{Payment, UserId}
import play.api.libs.iteratee.Enumerator

import scala.concurrent.Future

/**
  * [[Payment]] repository
  */
trait PaymentRepository {

  /**
    * Find all payments done by the customer
    *
    * @param user user identifier
    * @return all payments done by the user
    */
  def findByUser(user: UserId): Enumerator[Payment]

  /**
    * Saves payment for future reference
    *
    * @param payment payment to save
    * @return saved Payment presentation
    */
  def save(payment: Payment): Future[Payment]

}
