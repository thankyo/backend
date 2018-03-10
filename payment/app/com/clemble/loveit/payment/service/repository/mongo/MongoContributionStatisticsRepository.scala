package com.clemble.loveit.payment.service.repository.mongo

import javax.inject.{Inject, Named}

import akka.stream.Materializer
import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.payment.model.ContributionStatistics
import com.clemble.loveit.payment.service.repository.ContributionStatisticsRepository
import play.api.libs.json.{JsObject, Json}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.{ExecutionContext, Future}

class MongoContributionStatisticsRepository @Inject()(
  @Named("userPayment") collection: JSONCollection,
  implicit val m: Materializer,
  implicit val ec: ExecutionContext
) extends ContributionStatisticsRepository {

  import reactivemongo.play.json.commands.JSONAggregationFramework._

  override def find(user: UserID): Future[ContributionStatistics] = {
    val selector = Json.obj("_id" -> user)
    val projection = Json.obj("contributions" -> Json.obj("$size" -> "$charges"))
    collection.aggregate(Match(selector), List(Project(projection)))
      .map(_.firstBatch.headOption)
      .map(headOpt => {
        val contributions = headOpt.map(_ \ "contributions").flatMap(_.asOpt[Int]).getOrElse(0)
        ContributionStatistics(user, contributions)
      })
  }

}
