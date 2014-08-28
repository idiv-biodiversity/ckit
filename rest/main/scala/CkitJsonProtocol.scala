package ckit
package rest

import spray.json.DefaultJsonProtocol

import Binding._

object CkitJsonProtocol extends DefaultJsonProtocol {
  implicit val SimpleCoreBindingFormat =
    jsonFormat2(SimpleCoreBinding.apply)

  implicit val SimpleCoreBindingFormatE =
    eitherFormat[Int,SimpleCoreBinding]

  implicit val NodeInfoJobFormat =
    jsonFormat4(NodeInfo.Job)

  implicit val NodeInfoJobListFormat =
    listFormat(NodeInfoJobFormat)

  implicit val NodeInfoFormat =
    jsonFormat3(NodeInfo.apply)

  implicit val NodeInfoListFormat =
    listFormat(NodeInfoFormat)
}
