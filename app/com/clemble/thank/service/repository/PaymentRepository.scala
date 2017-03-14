package com.clemble.thank.service.repository

import akka.stream.scaladsl.Source
import com.clemble.thank.model.{Payment, UserId}

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
  def findByUser(user: UserId): Source[Payment, _]

  /**
    * Saves payment for future reference
    *
    * @param payment payment to save
    * @return saved Payment presentation
    */
  def save(payment: Payment): Future[Payment]

}
