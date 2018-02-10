package com.clemble.loveit.thank.model

import com.clemble.loveit.common.util.WriteableUtils
import play.api.http.Writeable
import play.api.libs.json._

sealed trait WebStack

case object WordPress extends WebStack

object WebStack {

  implicit val jsonFormat: Format[WebStack] = new Format[WebStack] {
    val WORD_PRESS = JsString("WordPress")

    override def writes(o: WebStack): JsValue = o match {
      case WordPress => WORD_PRESS
    }

    override def reads(json: JsValue): JsResult[WebStack] = json match {
      case WORD_PRESS => JsSuccess(WordPress)
    }
  }

  implicit val writeable: Writeable[WebStack] = WriteableUtils.jsonToWriteable[WebStack]

}
