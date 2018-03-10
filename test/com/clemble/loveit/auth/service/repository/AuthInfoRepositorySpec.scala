package com.clemble.loveit.auth.service.repository

import com.clemble.loveit.common.RepositorySpec
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class AuthInfoRepositorySpec(implicit ecEnv: ExecutionEnv ) extends RepositorySpec {

  val repo = dependency[AuthInfoRepository]

  "OAuthInfo update keeps refresh token" in {
    val loginInfo = LoginInfo(
      providerID = someRandom[String],
      providerKey = someRandom[String]
    )
    val info = OAuth2Info(
      accessToken = someRandom[String],
      refreshToken = Some(someRandom[String])
    )

    await(repo.add(loginInfo, info)) shouldEqual info

    val otherInfo = OAuth2Info(
      accessToken = someRandom[String]
    )
    val expectedInfo = OAuth2Info(
      accessToken = otherInfo.accessToken,
      refreshToken = info.refreshToken
    )

    await(repo.update(loginInfo, otherInfo)) shouldEqual expectedInfo
    await(repo.find(loginInfo)) shouldEqual Some(expectedInfo)
  }

}
