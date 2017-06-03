package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.user.model.User

class ROVerificationGeneratorSpec extends ServiceSpec {

  lazy val enc = dependency[ROVerificationGenerator]

  "Can encrypt and decrypt" in {
    val user = someRandom[User]
    val res = someRandom[Resource]

    val encStr = enc.encrypt(user.id, res)
    val (tagUser, tagRes) = enc.decrypt(encStr)

    tagUser shouldEqual user.id
    tagRes shouldEqual res
  }

}
