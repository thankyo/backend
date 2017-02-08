package com.clemble.thank.services

import com.clemble.thank.model.Thank

import scala.concurrent.Future

trait ThankService {

  def get(url: String): Future[Thank]

  def thank(url: String): Future[Thank]

}
