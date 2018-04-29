package com.clemble.loveit.thank.service

import com.clemble.loveit.common.model.{ResourceExtensions, _}
import com.clemble.loveit.common.ServiceSpec
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class EmailProjectOwnershipTokenServiceSpec extends ServiceSpec {

  lazy val emailVerService = dependency[EmailProjectOwnershipService]

  "CREATE" should {

    "Forbid creation if url & domain does not match" in {
      val user = createUser()

      val res = randomResource
      val email = s"some@${randomResource.toParentDomain()}"

      await(emailVerService.sendVerification(user, email)) should throwA[Exception]
    }

    "Allow creation in the same domain" in {
      val user = createUser()

      val res = randomResource
      val email = s"some@${res.toParentDomain()}"

      await(emailVerService.sendVerification(user, email)) should not(throwA[Exception])
    }

  }

  "Validation" should {

    "Forbid validation if user is different" in {
      val user = createUser()

      val res = randomResource
      val email = s"some@${res.toParentDomain()}"

      val token = await(emailVerService.sendVerification(user, email)).token

      val anotherUser = createUser()

      await(emailVerService.verify(anotherUser, token)) should throwA[Exception]
    }

    "Allow validation by the same user" in {

      val user = createUser()

      val res = randomResource
      val email = s"some@${res.toParentDomain()}"

      val token = await(emailVerService.sendVerification(user, email)).token

      await(emailVerService.verify(user, token)) shouldNotEqual None
    }

  }

}
