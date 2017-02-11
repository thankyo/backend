package com.clemble.thank.service

import com.clemble.thank.model.Thank
import com.clemble.thank.service.repository.ThankRepository
import com.google.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class SimpleThankService @Inject()(repository: ThankRepository, implicit val ec: ExecutionContext) extends ThankService {

  override def get(url: String): Future[Thank] = {
    def blankThank(url: String): Thank = Thank(url, 0)

    def createIfMissing(thankOpt: Option[Thank]): Future[Thank] = {
      thankOpt match {
        case Some(thank) => Future.successful(thank)
        case None => repository.create(blankThank(url))
      }
    }

    repository.findByUrl(url).flatMap(createIfMissing)
  }

  override def thank(url: String): Future[Thank] = {
    get(url).
      flatMap(_ => repository.increase(url)).
      filter(_ == true).
      flatMap(_ => repository.findByUrl(url).map(_.get))
  }

}
