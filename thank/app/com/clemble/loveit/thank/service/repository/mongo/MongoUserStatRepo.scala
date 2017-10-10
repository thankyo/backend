package com.clemble.loveit.thank.service.repository.mongo

import java.time.YearMonth
import javax.inject.{Inject, Named}

import com.clemble.loveit.common.model.UserID
import com.clemble.loveit.thank.model.{Thank, UserStat}
import com.clemble.loveit.thank.service.repository.UserStatRepo
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.{ExecutionContext, Future}

class MongoUserStatRepo @Inject()(
                                   @Named("stat") collection: JSONCollection,
                                   implicit val ec: ExecutionContext
) extends UserStatRepo {

  private def toStr(yearMonth: YearMonth) = {
    val yearMonth = YearMonth.now()
    val yearStr = yearMonth.getYear.toString
    val monthStr = yearMonth.getMonthValue.toString
    (yearStr -> monthStr)
  }

  override def increase(userID: UserID): Future[Boolean] = {
    val (yearStr, monthStr) = toStr(YearMonth.now())

    val selector = Json.obj("_id" -> userID)
    val update = Json.obj("$inc" -> Json.obj(s"${yearStr}.${monthStr}" -> 1))

    collection.update(selector, update).flatMap(wr => {
      if (wr.ok && wr.n == 1) {
        Future.successful(true)
      } else {
        collection.find(selector).one[JsObject].flatMap(_ match {
          case Some(_) =>
            collection.
              update(selector, Json.obj("$set" -> Json.obj(yearStr -> Json.obj(monthStr -> 1)))).
              map(_.ok)
          case None =>
            collection.
              insert(Json.obj("_id" -> userID, yearStr -> Json.obj(monthStr -> 1))).
              map(_.ok)
        })
      }
    })
  }

  override def get(user: UserID, yearMonth: YearMonth): Future[UserStat] = {
    val (yearStr, monthStr) = toStr(YearMonth.now())

    val selector = Json.obj("_id" -> user)
    val projection = Json.obj(s"${yearStr}.${monthStr}" -> 1);

    collection.find(selector, projection).
      one[JsObject].
      map(objOpt => {
        objOpt.flatMap(stat => (stat \ yearStr \ monthStr).asOpt[Int]) match {
          case Some(total) => UserStat(user, yearMonth, total)
          case None => UserStat(user, yearMonth)
        }
      })
  }

}
