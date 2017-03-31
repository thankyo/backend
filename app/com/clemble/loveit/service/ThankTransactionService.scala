package com.clemble.loveit.service

import akka.stream.scaladsl.Source
import com.clemble.loveit.model._

import scala.concurrent.Future

trait ThankTransactionService {

  def list(user: UserID): Source[ThankTransaction, _]

  def create(giver: UserID, url: Resource, amount: Amount): Future[List[ThankTransaction]]

}
