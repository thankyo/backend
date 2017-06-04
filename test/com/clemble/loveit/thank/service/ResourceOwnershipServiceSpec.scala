package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.{UserException}
import com.clemble.loveit.common.model.Resource
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
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
    await(service.assignOwnership(userAuth.head._2, resource))
  }

  "POST" should {

    "assign ownership" in {
      val social = someRandom[CommonSocialProfile]
      val userAuth = createUser(social)

      val resource = Resource from s"http://${RandomStringUtils.random(10)}.com"
      assignOwnership(userAuth, resource)

      val expectedResources = List(resource)
      eventually(listResources(userAuth) shouldEqual expectedResources)
    }

    "prohibit assigning same resource" in {
      val A = createUser()
      val B = createUser()

      val resource = someRandom[Resource]

      assignOwnership(A, resource) mustEqual resource
      assignOwnership(B, resource) must throwA[UserException]
    }

    "prohibit assigning sub resource" in {
      val A = createUser()
      val B = createUser()

      val child = someRandom[Resource]
      val parent = child.parent.get

      assignOwnership(A, parent) mustEqual parent
      assignOwnership(B, child) must throwA[UserException]
    }

    "allow assigning of sub resource to the owner" in {
      val A = createUser()

      val child = someRandom[Resource]
      val parent = child.parent.get

      assignOwnership(A, parent) mustEqual parent
      assignOwnership(A, child) mustEqual child
    }

  }

}
