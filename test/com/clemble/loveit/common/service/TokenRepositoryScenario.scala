package com.clemble.loveit.common.service

import com.clemble.loveit.auth.model.ResetPasswordToken
import com.clemble.loveit.auth.service.SimpleResetPasswordService
import com.clemble.loveit.common.RepositorySpec
import com.clemble.loveit.common.model.{Email, Token, UserID}
import com.clemble.loveit.thank.service.{EmailProjectOwnershipToken, SimpleEmailProjectOwnershipService}
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
trait TokenRepositoryScenario[T <: Token] extends RepositorySpec {

  val repo: TokenRepository[T]

  def newToken(user: UserID): T

  "REMOVES ON FIRST READ" in {
    val user = createUser()

    val verification = await(repo.save(newToken(user)))

    await(repo.findAndRemoveByToken(verification.token)) shouldEqual Some(verification)
    await(repo.findAndRemoveByToken(verification.token)) shouldEqual None
  }

  "REPLACES FOR THE USER" in {
    val user = createUser()

    val A = await(repo.save(newToken(user)))
    val B = await(repo.save(newToken(user)))

    await(repo.findAndRemoveByToken(A.token)) shouldEqual None
    await(repo.findAndRemoveByToken(B.token)) shouldEqual Some(B)
  }

}

@RunWith(classOf[JUnitRunner])
class ResetPasswordTokenRepositorySpec extends TokenRepositoryScenario[ResetPasswordToken] {

  val repo: TokenRepository[ResetPasswordToken] = dependency[SimpleResetPasswordService].repo

  override def newToken(user: UserID): ResetPasswordToken = ResetPasswordToken(user)

}

@RunWith(classOf[JUnitRunner])
class EmailProjectOwnershipTokenRepositorySpec extends TokenRepositoryScenario[EmailProjectOwnershipToken] {

  val repo: TokenRepository[EmailProjectOwnershipToken] = dependency[SimpleEmailProjectOwnershipService].repo

  override def newToken(user: UserID): EmailProjectOwnershipToken = EmailProjectOwnershipToken(user, someRandom[Email], randomResource)

}


