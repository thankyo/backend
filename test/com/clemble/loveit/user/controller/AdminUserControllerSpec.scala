package com.clemble.loveit.user.controller

import com.clemble.loveit.common.ControllerSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class AdminUserControllerSpec extends ControllerSpec {

  val admin = createUser()

  def count() = {
    val res = perform(admin, FakeRequest(GET, "/api/v1/user/stat/count"))
    res.body.dataStream.readJsonOpt[Int]().get
  }

  "count" in {
    count() shouldNotEqual 0
  }

}
