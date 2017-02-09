package com.clemble.thank.service

import com.clemble.thank.model.Amount

import scala.concurrent.Future

trait AdminPaymentService {

  def balance(): Future[Amount]

}
