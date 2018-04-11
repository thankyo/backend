package com.clemble.loveit.thank.service

import akka.actor.{Actor, ActorSystem, Props}
import com.clemble.loveit.common.model.OpenGraphObject
import akka.pattern.ask
import com.clemble.loveit.common.service._

class TestPostEventBusListener extends Actor {
  var events = List.empty[PostEvent]

  override def receive: Receive = {
    case postEvent: PostEvent =>
      events = postEvent :: events
    case TestPostEventBusListener.GET_POSTS =>
      sender() ! events
  }

}

object TestPostEventBusListener {

  val GET_POSTS = "GET_POSTS"

}


class PostEventBusSpec extends InternalPostTestService {

  lazy val actorSystem = dependency[ActorSystem]
  lazy val postEventBus = dependency[PostEventBus]

  val eventListener = {
    val listener = actorSystem.actorOf(Props[TestPostEventBusListener]())
    postEventBus.subscribe(listener, classOf[PostEvent])
    listener
  }

  def getPosts() = {
    (eventListener ? TestPostEventBusListener.GET_POSTS).mapTo[List[PostEvent]]
  }

  "Created event" in {
    val prj = createProject()
    val post = createPost(someRandom[OpenGraphObject].copy(url = prj.url))

    eventually(await(getPosts) should containAllOf(Seq(PostCreated(post))))
  }

  "Updated event" in {
    val prj = createProject()

    val oldPost = createPost(someRandom[OpenGraphObject].copy(url = prj.url))
    val updatePost = createPost(someRandom[OpenGraphObject].copy(url = prj.url))

    eventually(await(getPosts) should containAllOf(Seq(PostUpdated(oldPost, updatePost))))
  }

  "Deleted post event" in {
    val prj = createProject()

    val post = createPost(someRandom[OpenGraphObject].copy(url = prj.url))
    await(postService.delete(post._id))

    eventually(await(getPosts) should containAllOf(Seq(PostRemoved(post))))
  }

  "Deleted project event" in {
    val prj = createProject()

    val posts = 1 to 10 map(_ => createPost(someRandom[OpenGraphObject].copy(url = someChildResource(prj.url))))
    await(postService.delete(prj))

    val deleteEvents = posts.map(PostRemoved)
    eventually(await(getPosts) should containAllOf(deleteEvents))
  }

}
