package com.clemble.loveit.thank.service

import javax.inject.Singleton

import akka.actor.ActorRef
import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import com.clemble.loveit.common.model.ThankTransaction

@Singleton
class ThankEventBus extends ActorEventBus with SubchannelClassification {

  override protected implicit def subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y

    def isSubclass(x: Classifier, y: Classifier): Boolean = y.isAssignableFrom(x)
  }

  override def classify(event: ThankTransaction): Classifier = {
    event.getClass
  }

  override def publish(event: ThankTransaction, subscriber: ActorRef): Unit = {
    subscriber ! event
  }

  override type Event = ThankTransaction
  override type Classifier = Class[_ <: ThankTransaction]

}
