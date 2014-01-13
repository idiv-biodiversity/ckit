package ckit
package rest

import spray.json.DefaultJsonProtocol

object CkitJsonProtocol extends DefaultJsonProtocol {
  implicit val NodeInfoJobFormat =
    jsonFormat4(NodeInfo.Job)

  implicit val NodeInfoJobListFormat =
    listFormat(NodeInfoJobFormat)

  implicit val NodeInfoFormat =
    jsonFormat3(NodeInfo.apply)
}
