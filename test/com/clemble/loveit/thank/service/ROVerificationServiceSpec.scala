package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.common.util.IDGenerator
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ROVerificationServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec  {

  lazy val resVerService = dependency[ROVerificationService]

  "CREATE" should {

    "ignore if resource already owned" in {
      val A = someUser()
      val B = someUser()

      val res = someRandom[Resource]
      assignOwnership(A.id, res)

      await(resVerService.create(B.id, res)) should throwA[UserException]
    }

    "simply works" in {
      val A = someUser()
      val res = someRandom[Resource]

      await(resVerService.create(A.id, res)).resource shouldEqual res
    }

  }

}
