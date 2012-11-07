/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                               *
 *  Copyright  ©  2012  Christian Krause                                                         *
 *                                                                                               *
 *  Christian Krause  <christian.krause@ufz.de>                                                  *
 *                    <kizkizzbangbang@googlemail.com>                                           *
 *                                                                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *                                                                                               *
 *  This file is part of 'ClusterKit'.                                                           *
 *                                                                                               *
 *  This project is free software: you can redistribute it and/or modify it under the terms      *
 *  of the GNU General Public License as published by the Free Software Foundation, either       *
 *  version 3 of the License, or any later version.                                              *
 *                                                                                               *
 *  This project is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;    *
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.    *
 *  See the GNU General Public License for more details.                                         *
 *                                                                                               *
 *  You should have received a copy of the GNU General Public License along with this project.   *
 *  If not, see <http://www.gnu.org/licenses/>.                                                  *
 *                                                                                               *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */


package ckit

import scala.util.Try
import scala.xml._

object GridEngine {

  private[GridEngine] val QueueInstance = """(.+)@(.+)""".r

  def jobList(xml: ⇒ Elem): Try[Seq[Job]] = Try {
    xml \\ "job_list" map job
  }

  def queueSummary(xml: ⇒ Elem): Try[Seq[QueueSummary]] = Try {
    xml \ "cluster_queue_summary" map { xml ⇒
      QueueSummary (
        name                 = (xml \ "name").text,
        load                 = (xml \ "load").text.toDouble,
        used                 = (xml \ "used").text.toInt,
        reserved             = (xml \ "resv").text.toInt,
        available            = (xml \ "available").text.toInt,
        total                = (xml \ "total").text.toInt,
        temporaryUnavailable = (xml \ "temp_disabled").text.toInt,
        unavailable          = (xml \ "manual_intervention").text.toInt
      )
    }
  }

  def runtimeSchedule(xml: ⇒ Elem): Try[Seq[(Job,Int)]] = Try {
    xml \ "queue_info" \ "job_list" map { xml ⇒
      (job(xml), jobResourceList(xml, "h_rt"))
    } collect {
      case (job,Some(runtime)) ⇒ (job,runtime.toInt)
    }
  }

  def jobResourceList(xml: Node, resource: String): Option[String] = xml \ "hard_request" collectFirst {
    case xml if (xml \ "@name").text == resource ⇒ xml.text
  }

  def job(xml: Node): Job = {
    val (q,n) = (xml \ "queue_name").text.trim match {
      case QueueInstance(q,n) ⇒ (q,n)
      case _                  ⇒ ("","")
    }

    Job (
      id       = (xml \ "JB_job_number").text.toInt,
      priority = (xml \ "JAT_prio").text.toDouble,
      name     = (xml \ "JB_name").text,
      owner    = (xml \ "JB_owner").text,
      state    = (xml \ "state").text,
      start    = (xml \ "JAT_start_time").text,
      queue    = q,
      node     = n,
      slots    = (xml \ "slots").text.toInt
    )
  }

}
