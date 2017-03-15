package com.clemble.thank.service

import com.clemble.thank.model.{Resource, Thank, UserID}

import scala.concurrent.Future

trait ThankService {

  def getOrCreate(uri: Resource): Future[Thank]

  def thank(user: UserID, uri: Resource): Future[Thank]

}
