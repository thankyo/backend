package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{ResourceExtensions, _}
import com.clemble.loveit.common.ServiceSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EmailVerificationTokenServiceSpec extends ServiceSpec {

  lazy val emailVerService = dependency[EmailVerificationTokenService]

  "CREATE" should {

    "Forbid creation if url & domain does not match" in {
      val user = createUser()

      val res = randomResource
      val email = s"some@${randomResource.toParentDomain()}"

      await(emailVerService.create(user, email, res)) should throwA[Exception]
    }

    "Allow creation in the same domain" in {
      val user = createUser()

      val res = randomResource
      val email = s"some@${res.toParentDomain()}"

      await(emailVerService.create(user, email, res)) should not(throwA[Exception])
    }

  }

  "Validation" should {

    "Forbid creation if user is different" in {
      val user = createUser()

      val res = randomResource
      val email = s"some@${res.toParentDomain()}"

      val token = await(emailVerService.create(user, email, res)).token

      val anotherUser = createUser()

      await(emailVerService.validate(anotherUser, token)) should throwA[Exception]
    }

    "Allow validation by the same user" in {

      val user = createUser()

      val res = randomResource
      val email = s"some@${res.toParentDomain()}"

      val token = await(emailVerService.create(user, email, res)).token

      await(emailVerService.validate(user, token)) shouldNotEqual None
    }

  }

}
