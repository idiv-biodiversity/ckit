package ckit
package rest

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.util._

import spray.httpx.SprayJsonSupport._
import spray.routing.SimpleRoutingApp

import ckit.daemon.GridEngineActor
import CkitJsonProtocol._

object CkitRestApp extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("ckit-rest")

  val ckit = system.actorOf(Props[GridEngineActor], "ckit-daemon-actor")

  implicit val timeout = Timeout(900.seconds)

  startServer(interface = "0.0.0.0", port = 8080) {
    get {
      path("rjm" / "api" / "host" / Rest) { node =>
        complete {
          implicit def executionContext = actorRefFactory.dispatcher

          ckit ? Protocol.NodeInfo(node) map {
            case info: NodeInfo => info
          }
        }
      } ~
      path("rjm" / "api" / "schedule") {
        complete {
          implicit def executionContext = actorRefFactory.dispatcher

          ckit ? Protocol.NodeInfoList map {
            case ListNodeInfo(nodes) => nodes
          }
        }
      } ~
      path("rjm" / "api" / "hosts") {
        complete {
          implicit def executionContext = actorRefFactory.dispatcher

          ckit ? Protocol.ExecHosts map {
            case NodeList(nodes) => nodes
          }
        }
      }
    }
  }
}
