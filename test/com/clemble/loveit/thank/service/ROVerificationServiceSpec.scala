package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.{ResourceException, UserException}
import com.clemble.loveit.common.model.Resource
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ROVerificationServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec  {

  lazy val resVerService = dependency[ROVerificationService]

  "CREATE" should {

    "ignore if resource already owned" in {
      val A = createUser()
      val B = createUser()

      val res = someRandom[Resource]
      assignOwnership(A, res)

      await(resVerService.create(B, res)) should throwA[UserException]
    }

    "simply works" in {
      val A = createUser()
      val res = someRandom[Resource]

      await(resVerService.create(A, res)).resource shouldEqual res
    }

    "error on already verifying resource" in {
      val A = createUser()
      val B = createUser()

      val res = someRandom[Resource]
      await(resVerService.create(A, res))

      await(resVerService.create(B, res)) should throwA[ResourceException]
    }

  }

}
