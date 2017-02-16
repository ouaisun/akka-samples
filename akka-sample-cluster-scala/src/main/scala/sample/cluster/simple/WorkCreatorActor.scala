package sample.cluster.simple

import scala.concurrent._
import scala.concurrent.duration._

import scala.language.postfixOps

import akka.actor._
import akka.routing._

object WorkCreatorActor {
  case object CreateWork
  case class AddWorker(worker: ActorRef)
}

class WorkCreatorActor extends Actor with ActorLogging {
  import WorkCreatorActor._

  implicit val ec: ExecutionContext = context.dispatcher
  context.system.scheduler.schedule(0 seconds, 2 seconds, self, CreateWork)

  var routees: List[ActorRef] = List()

  var router = newGroup(routees)

  def newGroup(routees: List[ActorRef]): ActorRef = {
    val paths = routees.map(_.path)
    // val group = RoundRobinGroup(List(s"${self.path}/worker-*"))
    // println(s"Paths for group are ${group.paths}")
    val group = RoundRobinGroup(paths.map(_.toString))
    context.actorOf(group.props)
  }

  override def receive = {
    case AddWorker(worker) =>
      routees = worker +: routees
      router = newGroup(routees)
    case CreateWork =>
      println("creating work")
      // println(s"Current routees: ${router.routees}")
      router ! "this is work"
  }
}
