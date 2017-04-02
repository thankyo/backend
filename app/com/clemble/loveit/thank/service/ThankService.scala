package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{Resource, UserID}
import com.clemble.loveit.thank.model.Thank

import scala.concurrent.Future

trait ThankService {

  def getOrCreate(uri: Resource): Future[Thank]

  def thank(user: UserID, uri: Resource): Future[Thank]

}
