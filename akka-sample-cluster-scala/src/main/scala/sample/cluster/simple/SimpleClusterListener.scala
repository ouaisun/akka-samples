package sample.cluster.simple

import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor._
import akka.routing._

import akka.actor.{ Props, Deploy, Address, AddressFromURIString }
import akka.remote.RemoteScope

class SimpleClusterListener(workCreator: Option[ActorRef]) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }
  override def postStop(): Unit = cluster.unsubscribe(self)

  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
      workCreator.map { ref =>
        (0 to 5).map { n =>
          val r = context.actorOf(Props[Worker].withDeploy(Deploy(scope = RemoteScope(member.address))))
          ref ! WorkCreatorActor.AddWorker(ActorRefRoutee(r).ref)
        }
      }
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }
}
