package com.clemble.loveit.thank.controller

import akka.stream.scaladsl.Sink
import com.clemble.loveit.common.error.ThankException
import com.clemble.loveit.common.model.{Resource}
import com.clemble.loveit.controller.ControllerSpec
import com.clemble.loveit.test.util.{CommonSocialProfileGenerator}
import com.clemble.loveit.thank.model.ResourceOwnership
import org.apache.commons.lang3.RandomStringUtils
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.libs.json.Json
import play.api.test.FakeRequest

@RunWith(classOf[JUnitRunner])
class ResourceOwnershipControllerSpec extends ControllerSpec {

  def listResources(userAuth: Seq[(String, String)]): Seq[ResourceOwnership] = {
    val req = FakeRequest(GET, s"/api/v1/thank/ownership/my").withHeaders(userAuth:_*)
    val fRes = route(application, req).get

    val res = await(fRes)
    val respSource = res.body.dataStream.map(byteStream => Json.parse(byteStream.utf8String).as[ResourceOwnership])
    await(respSource.runWith(Sink.seq[ResourceOwnership]))
  }

  def assignOwnership(userAuth: Seq[(String, String)], resource: ResourceOwnership): ResourceOwnership = {
    val req = FakeRequest(POST, s"/api/v1/thank/ownership/my").withHeaders(userAuth:_*).withJsonBody(Json.toJson(resource))
    val fRes = route(application, req).get

    val res = await(fRes)
    val fResp = res.body.consumeData.map(_.utf8String).map(Json.parse(_).as[ResourceOwnership])
    await(fResp)
  }

  "GET" should {

    "List on new user" in {
      val social = CommonSocialProfileGenerator.generate()
      val userAuth = createUser(social)

      val resources = listResources(userAuth)
      resources shouldEqual List(ResourceOwnership.full(Resource from social.loginInfo))
    }

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
