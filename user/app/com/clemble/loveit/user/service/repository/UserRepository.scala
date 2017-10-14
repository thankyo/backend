package com.clemble.loveit.user.service.repository

import akka.stream.scaladsl.Source
import com.clemble.loveit.common.error.RepositoryException
import com.clemble.loveit.common.model.{Email, UserID}
import com.clemble.loveit.user.model._
import com.mohiva.play.silhouette.api.LoginInfo

import scala.concurrent.Future

/**
  * [[User]] repository
  */
trait UserRepository {

  def retrieve(loginInfo: LoginInfo): Future[Option[User]]

  /**
    * Find [[User]] by ID
    */
  def findById(id: UserID): Future[Option[User]]

  /**
    * Find user by email
    */
  def findByEmail(email: Email): Future[Option[User]]

  /**
    * Create a new user in the system
    *
    * @param user user to create
    * @return saved user
    * @throws RepositoryException in case of duplicate code or any other problem
    */
  @throws[RepositoryException]
  def save(user: User): Future[User]

  /**
    * Update existing user
    */
  def update(user: User): Future[User]

  /**
    * Remove users
    */
  def remove(users: Seq[UserID]): Future[Boolean]

  /**
    * Find all users in the system
    */
  def find(): Source[User, _]

  /**
    * Count number of users
    */
  def count(): Future[Int]

}
