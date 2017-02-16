package sample.cluster.simple

import scala.concurrent._
import scala.concurrent.duration._

import scala.language.postfixOps

import akka.actor._

object Worker {
  case object CreateWork
}

class Worker extends Actor {
  import Worker._

  override def receive = {
    case something =>
      println(s"TODO actually do work '$something' at ${self.path}")
  }
}
