package com.clemble.loveit.common.service

import akka.actor.ActorRef
import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import com.clemble.loveit.common.model.{Post}

sealed trait PostEvent
case class PostCreated(post: Post)
case class PostUpdated(old: Post, newPost: Post)
case class PostRemoved(post: Post)

class PostEventBus extends ActorEventBus with SubchannelClassification {

  override protected implicit def subclassification: Subclassification[Classifier] = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y

    def isSubclass(x: Classifier, y: Classifier): Boolean = y.isAssignableFrom(x)
  }

  override def classify(event: PostEvent): Classifier = {
    event.getClass
  }

  override def publish(event: PostEvent, subscriber: ActorRef): Unit = {
    subscriber ! event
  }

  override type Event = PostEvent
  override type Classifier = Class[_ <: PostEvent]

}
