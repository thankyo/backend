package com.clemble.loveit.common.service

import akka.actor.ActorRef
import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import com.clemble.loveit.common.model.ThankEvent
import javax.inject.Singleton

@Singleton
class ThankEventBus extends ActorEventBus with SubchannelClassification {

  override protected implicit def subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y

    def isSubclass(x: Classifier, y: Classifier): Boolean = y.isAssignableFrom(x)
  }

  override def classify(event: ThankEvent): Classifier = {
    event.getClass
  }

  override def publish(event: ThankEvent, subscriber: ActorRef): Unit = {
    subscriber ! event
  }

  override type Event = ThankEvent
  override type Classifier = Class[_ <: ThankEvent]

}
