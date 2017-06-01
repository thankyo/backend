package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.ThankException
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.test.util.CommonSocialProfileGenerator
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ResourceOwnershipServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  lazy val service = dependency[ResourceOwnershipService]

  def listResources(userAuth: Seq[(String, String)]): Set[Resource] = {
    await(service.list(userAuth.head._2))
  }

  def assignOwnership(userAuth: Seq[(String, String)], resource: Resource) = {
    await(service.assign(userAuth.head._2, resource))
  }

  "POST" should {

    "assign ownership" in {
      val social = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(social)

      val resource = Resource from s"http://${RandomStringUtils.random(10)}.com"
      assignOwnership(userAuth, resource)

      val expectedResources = List(
        Resource from social.loginInfo,
        resource
      )
      eventually(listResources(userAuth) shouldEqual expectedResources)
    }

    "prohibit assigning same resource" in {
      val A = createUser()
      val B = createUser()

      val resource = Resource from s"http://${RandomStringUtils.random(10)}.com"

      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, resource) must throwA[ThankException]
    }

    "prohibit assigning sub resource" in {
      val A = createUser()
      val B = createUser()

      val uri = s"http://${RandomStringUtils.random(10)}.com/"

      val resource = Resource from uri
      val subResource = Resource from s"$uri/${RandomStringUtils.random(10)}"

      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, subResource) must throwA[ThankException]
    }

    "allow assigning of sub resource to the owner" in {
      val A = createUser()

      val uri = s"http://${RandomStringUtils.random(10)}.com/"

      val resource = Resource from uri
      val subResource = Resource from s"$uri/${RandomStringUtils.random(10)}"

      assignOwnership(A, resource) mustEqual resource
      assignOwnership(A, subResource) mustEqual subResource
    }

  }

}
