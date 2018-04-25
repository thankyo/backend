package com.clemble.loveit.common.service

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.common.RepositorySpec
import com.google.inject.name.{Named, Names}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.inject.{BindingKey, QualifierInstance}

@RunWith(classOf[JUnitRunner])
class TokenRepositorySpec extends RepositorySpec {

  val repo: TokenRepository[ResetPasswordToken] = dependency(
    BindingKey(classOf[TokenRepository[ResetPasswordToken]])
      .qualifiedWith("resetPasswordTokenRepo")
  )

  "REMOVES ON FIRST READ" in {
    val user = createUser()

    val verification = await(repo.save(ResetPasswordToken(user)))

    await(repo.findAndRemoveByToken(verification.token)) shouldEqual Some(verification)
    await(repo.findAndRemoveByToken(verification.token)) shouldEqual None
  }

  "REPLACES FOR THE USER" in {
    val user = createUser()

    val A = await(repo.save(ResetPasswordToken(user)))
    val B = await(repo.save(ResetPasswordToken(user)))

    await(repo.findAndRemoveByToken(A.token)) shouldEqual None
    await(repo.findAndRemoveByToken(B.token)) shouldEqual Some(B)
  }

}
