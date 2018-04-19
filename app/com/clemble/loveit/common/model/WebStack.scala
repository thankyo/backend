package com.clemble.loveit.common.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json._

sealed trait WebStack

case object WordPress extends WebStack
case object Tumblr extends WebStack
case object LandingLion extends WebStack

object WebStack {

  implicit val jsonFormat: Format[WebStack] = new Format[WebStack] {
    val WORD_PRESS = JsString("WordPress")
    val TUMBLR = JsString("Tumblr")
    val LANDING_LION = JsString("LandingLion")

    override def writes(o: WebStack): JsValue = o match {
      case WordPress => WORD_PRESS
      case Tumblr => TUMBLR
    }

    override def reads(json: JsValue): JsResult[WebStack] = json match {
      case WORD_PRESS => JsSuccess(WordPress)
      case TUMBLR => JsSuccess(Tumblr)
      case LANDING_LION => JsSuccess(LandingLion)
      case _ => JsError("No WebStack associated")
    }
  }

  implicit val writeable: Writeable[WebStack] = WriteableUtils.jsonToWriteable[WebStack]

}
