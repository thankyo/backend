package com.clemble.loveit.common

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import play.api.Mode
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Reads, Writes}
import play.api.test.PlaySpecification

import scala.collection.immutable
import scala.reflect.ClassTag

trait ThankSpecification extends PlaySpecification {

  implicit lazy val application = ThankSpecification.application

  def dependency[T: ClassTag]: T = {
    application.injector.instanceOf[T]
  }

  import com.clemble.loveit.test.util._
  def someRandom[T](implicit generator: Generator[T]) = generator.generate()

  def view[S, V](source: S)(implicit sWrites: Writes[S], vReads: Reads[V]): V = {
    vReads.reads(sWrites.writes(source)).get
  }

  implicit lazy val materializer: Materializer = dependency[Materializer]

  implicit class SourceToList[T](source: Source[T, _]) {
    def toSeq(): immutable.Seq[T] = {
      await(source.runWith(Sink.seq[T]))
    }
  }

}

object ThankSpecification {

  lazy val application = {
    val app = new GuiceApplicationBuilder().
      in(Mode.Test).
      build
    app
  }

}
