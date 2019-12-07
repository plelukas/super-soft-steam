package com.synerise.steam.master.actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props, RootActorPath}
import akka.cluster.ClusterEvent._
import akka.cluster.{Cluster, Member}
import com.synerise.steam.master.{WorkerJoined, WorkerRemoved}

class MasterClusterListener(cluster: Cluster, masterStateManager: ActorRef) extends Actor with ActorLogging {

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents, classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }

  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive: Receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      registerWorker(member)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
      unregisterWorker(member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}", member.address, previousStatus)
      unregisterWorker(member)
    case _: MemberEvent => // ignore
  }

  private def registerWorker(member: Member) = {
    if (member.hasRole("worker")) {
      val worker = context.actorSelection(RootActorPath(member.address) / "user" / "state-manager")
      log.info(s"Detected worker ($member). Sending worker joined message")
      masterStateManager ! WorkerJoined(member, worker)
    }
  }

  private def unregisterWorker(member: Member) = {
    if (member.hasRole("worker")) {
      log.info(s"Worker ($member) down. Sending worker removed message")
      masterStateManager ! WorkerRemoved(member)
    }
  }
}

object MasterClusterListener {

  def props(cluster: Cluster, masterStateManager: ActorRef) = Props(new MasterClusterListener(cluster, masterStateManager))


}
