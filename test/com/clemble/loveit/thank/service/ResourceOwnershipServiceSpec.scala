package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.ThankException
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.test.util.CommonSocialProfileGenerator
import com.clemble.loveit.thank.model.ResourceOwnership
import org.apache.commons.lang3.RandomStringUtils

class ResourceOwnershipServiceSpec extends ServiceSpec {

  lazy val service = dependency[ResourceOwnershipService]

  def listResources(userAuth: Seq[(String, String)]): Set[ResourceOwnership] = {
    await(service.listMy(userAuth.head._2))
  }

  def assignOwnership(userAuth: Seq[(String, String)], resource: ResourceOwnership): ResourceOwnership = {
    await(service.assign(userAuth.head._2, resource))
  }

  "POST" should {

    "assign partial ownership" in {
      val social = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(social)

      val resource = ResourceOwnership.partial(Resource from s"http://${RandomStringUtils.random(10)}.com")
      assignOwnership(userAuth, resource)

      val userResources = listResources(userAuth)
      userResources shouldEqual List(
        ResourceOwnership.full(Resource from social.loginInfo),
        resource
      )
    }

    "assign full ownership" in {
      val social = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(social)

      val resource = ResourceOwnership.full(Resource from s"http://${RandomStringUtils.random(10)}.com")
      assignOwnership(userAuth, resource)

      val userResources = listResources(userAuth)
      userResources shouldEqual List(
        ResourceOwnership.full(Resource from social.loginInfo),
        resource
      )
    }

    "prohibit assigning same resource" in {
      val A = createUser()
      val B = createUser()

      val resource = ResourceOwnership.full(Resource from s"http://${RandomStringUtils.random(10)}.com")
      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, resource) must throwA[ThankException]
    }

    "prohibit assigning sub resource in case full ownership" in {
      val A = createUser()
      val B = createUser()

      val uri = s"http://${RandomStringUtils.random(10)}.com/"

      val resource = ResourceOwnership.full(Resource from uri)
      val subResource = ResourceOwnership.full(Resource from s"$uri/${RandomStringUtils.random(10)}")
      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, subResource) must throwA[ThankException]
    }

    "prohibit assigning sub resource in case of partial ownership" in {
      val A = createUser()
      val B = createUser()

      val uri = s"http://${RandomStringUtils.random(10)}.com/"

      val resource = ResourceOwnership.partial(Resource from uri)
      val subResource = ResourceOwnership.full(Resource from s"$uri/${RandomStringUtils.random(10)}")
      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, subResource) mustEqual subResource
    }

  }

}
