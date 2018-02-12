package com.clemble.loveit.thank.service

import com.clemble.loveit.common.ServiceSpec
import com.clemble.loveit.common.error.UserException
import com.clemble.loveit.common.model.Resource
import com.clemble.loveit.thank.model.Project
import org.junit.runner.RunWith
import org.specs2.concurrent.ExecutionEnv
import org.specs2.runner.JUnitRunner

@RunWith(classOf[JUnitRunner])
class OwnedProjectServiceSpec(implicit val ee: ExecutionEnv) extends ServiceSpec {

  lazy val service = dependency[OwnedProjectService]
  lazy val supPrjService = dependency[ProjectService]

  def listResources(user: String): Set[Resource] = {
    await(supPrjService.findProjectsByUser(user)).map(_.resource).toSet
  }

  def assignOwnership(userAuth: Seq[(String, String)], resource: Resource) = {
    val user = userAuth.head._2
    await(service.enable(Project(resource, user)))
  }

  "POST" should {

    "assign ownership" in {
      val user = createUser()

      val resource = Resource from s"http://${someRandom[String]}.com"
      createProject(user, resource)

      eventually(listResources(user).size shouldEqual 1)

      val expectedResources = Set(resource)
      val actualResources = listResources(user)
      actualResources mustEqual expectedResources
    }

    "allow assigning of sub resource to the owner" in {
      val A = createUser()

      val child = someRandom[Resource]
      val parent = child.parent.get

      createProject(A, parent).resource mustEqual parent
      createProject(A, child).resource mustEqual child
    }

  }

// TODO restore
//  "CREATION RESTRICTION" in {
//
//    "prohibit assigning same resource" in {
//      val A = createUser()
//      val B = createUser()
//
//      val resource = someRandom[Resource]
//
//      createProject(A, resource).resource mustEqual resource
//      createProject(B, resource) must throwA[UserException]
//    }
//
//    "prohibit assigning sub resource" in {
//      val A = createUser()
//      val B = createUser()
//
//      val child = someRandom[Resource]
//      val parent = child.parent.get
//
//      createProject(A, parent).resource mustEqual parent
//      createProject(B, child) must throwA[UserException]
//    }
//
//  }

}
