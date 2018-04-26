package com.clemble.loveit.thank.service

import akka.actor.{Actor, Props}
import com.clemble.loveit.common.model.{OwnedProject, Resource, User, UserID}
import com.clemble.loveit.common.service.{URLValidator, WSClientAware}
import com.clemble.loveit.common.util.EventBusManager
import com.clemble.loveit.thank.model.UserProjects
import com.clemble.loveit.thank.service.repository.UserProjectsRepository
import com.mohiva.play.silhouette.api.{LoginEvent, SignUpEvent}
import javax.inject.{Inject, Singleton}
import com.clemble.loveit.common.error.FieldValidationError

import scala.concurrent.{ExecutionContext, Future}

trait UserProjectsService {

  def get(user: UserID): Future[UserProjects]

  def dibsOnUrl(user: UserID, url: Resource): Future[OwnedProject]

  def create(user: User): Future[UserProjects]

  def deleteOwned(user: UserID, url: Resource): Future[UserProjects]

  def updateOwned(user: UserID): Future[UserProjects]

}

case class UserProjectsServiceSignUpListener @Inject()(uPrjS: UserProjectsService) extends Actor {

  override def receive: Receive = {
    case SignUpEvent(user: User, _) =>
      uPrjS.create(user)
    case LoginEvent(user: User, _) =>
      // TODO this will trigger ownership revalidation on every log in, which is very bad need to optimize this
      uPrjS.updateOwned(user.id)
  }

}

@Singleton
case class SimpleUserProjectsService @Inject()(
  eventBusManager: EventBusManager,
  projectEnrichService: ProjectEnrichService,
  ownershipService: ProjectOwnershipService,
  emailVerSvc: EmailVerificationTokenService,
  urlValidator: URLValidator,
  repo: UserProjectsRepository,
  implicit val ec: ExecutionContext
) extends UserProjectsService {

  eventBusManager.onSignUp(Props(UserProjectsServiceSignUpListener(this)))
  eventBusManager.onLogin(Props(UserProjectsServiceSignUpListener(this)))

  override def create(user: User): Future[UserProjects] = {
    val projects = UserProjects(user.id, Seq.empty, Seq.empty)
    repo.save(projects)
  }

  override def dibsOnUrl(user: UserID, url: Resource): Future[OwnedProject] = {
    for {
      urlOpt <- urlValidator.findAlive(url)
      _ = if (urlOpt.isEmpty) throw FieldValidationError("url", "Can't connect")
      ownedProject <- projectEnrichService.enrich(user, urlOpt.get)
      _ <- repo.saveOwnedProject(user, Seq(ownedProject))
    } yield {
      emailVerSvc.verifyWithWHOIS(user, urlOpt.get)
      ownedProject
    }
  }

  override def get(user: UserID): Future[UserProjects] = {
    repo.findById(user).map(_.get)
  }

  override def updateOwned(user: UserID): Future[UserProjects] = {
    ownershipService
      .fetch(user)
      .flatMap(owned => {
        repo.saveOwnedProject(user, owned)
      })
  }

  override def deleteOwned(user: UserID, url: Resource): Future[UserProjects] = {
    for {
      installedPrjOpt <- repo.findProjectByUrl(url)
      _ = if(installedPrjOpt.isDefined) throw new IllegalArgumentException("Remove installed project first")
      userProject <- repo.deleteOwnedProject(user, url)
    } yield {
      userProject
    }
  }

}
