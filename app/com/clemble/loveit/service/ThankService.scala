package com.clemble.loveit.service

import com.clemble.loveit.model.{Resource, Thank, UserID}

import scala.concurrent.Future

trait ThankService {

  def getOrCreate(uri: Resource): Future[Thank]

  def thank(user: UserID, uri: Resource): Future[Thank]

}
