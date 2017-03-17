package com.clemble.thank.service

import com.clemble.thank.model.{BankDetails, UserID}

trait PaymentService {

  def receive(user: UserID, bankDetails: BankDetails, amount: Int)

  def withdraw(user: UserID, bankDetails: BankDetails, amount: Int)

}
