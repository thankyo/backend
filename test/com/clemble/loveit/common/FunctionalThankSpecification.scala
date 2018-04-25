package com.clemble.loveit.common

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.clemble.loveit.common.service.TokenRepository
import javax.inject.Named
import play.api.Mode
import play.api.inject.BindingKey
import play.api.inject.guice.GuiceApplicationBuilder

import scala.collection.immutable
import scala.concurrent.ExecutionContext
import scala.reflect.ClassTag

trait FunctionalThankSpecification extends ThankSpecification with ThankExecutor {

  implicit lazy val application = FunctionalThankSpecification.application

  def dependency[T: ClassTag]: T = {
    application.injector.instanceOf[T]
  }

  def dependency[T](bindingKey: BindingKey[T]): T = {
    application.injector.instanceOf[T](bindingKey)
  }

  implicit lazy val materializer: Materializer = dependency[Materializer]

  implicit class SourceToList[T](source: Source[T, _]) {
    def toSeq(): immutable.Seq[T] = {
      await(source.runWith(Sink.seq[T]))
    }
  }

}

object FunctionalThankSpecification {

  lazy val application = {
    val app = new GuiceApplicationBuilder().
      in(Mode.Test).
      build
    app
  }

}
