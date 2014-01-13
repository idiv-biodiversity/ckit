package ckit
package rest

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import spray.httpx.SprayJsonSupport._
import spray.routing.SimpleRoutingApp

import ckit.daemon.GridEngineActor
import CkitJsonProtocol._

object CkitRestApp extends App with SimpleRoutingApp {
  implicit val system = ActorSystem("ckit-rest-api-system")

  val ckit = system.actorOf(Props[GridEngineActor], "ckit-daemon-actor")

  implicit val timeout = Timeout(15.seconds)

  startServer(interface = "0.0.0.0", port = 8080) {
    get {
      path("rjm" / "api" / "host" / Rest) { node =>
        complete {
          import reflect.ClassTag
          implicit def executionContext = actorRefFactory.dispatcher

          (ckit ? Protocol.NodeInfo(node)).mapTo[NodeInfo]
        }
      }
    }
  }
}
