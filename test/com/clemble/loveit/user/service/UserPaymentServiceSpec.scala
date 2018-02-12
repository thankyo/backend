package com.clemble.loveit.user.service

import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.payment.service.PaymentServiceTestExecutor
import com.clemble.loveit.thank.model.Project
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class UserPaymentServiceSpec(implicit ee: ExecutionEnv) extends PaymentServiceTestExecutor {

  val giver = createUser()

  def childProject(project: Project): Project = {
    val user = createUser()
    val childResource = Resource.from(s"${project.resource.stringify()}/${someRandom[Long]}")
    createProject(user, childResource)
  }

  "UPDATE OWNER BALANCE single hierarchy" in {
    val project = createProject()

    thank(giver, project)
    getBalance(project.user) shouldEqual 1
  }

  "OWNER BALANCE with child hierarchy" in {
    val parentProject = createProject()

    val subProject = childProject(parentProject)

    thank(giver, subProject)
    getBalance(subProject.user) shouldEqual 1
    getBalance(parentProject.user) shouldEqual 0
  }

}
