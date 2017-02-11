package com.clemble.thank.service

import com.clemble.thank.model.{Thank, UserId}

import scala.concurrent.Future

trait ThankService {

  def get(url: String): Future[Thank]

  def thank(user: UserId, url: String): Future[Thank]

}
