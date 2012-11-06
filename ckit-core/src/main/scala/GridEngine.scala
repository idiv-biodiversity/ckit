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

  val QueueInstance = """(.+)@(.+)""".r

  def parseJobListFromXML(xml: ⇒ Elem): Try[Seq[Job]] = Try {
    xml \\ "job_list" map { xml ⇒
      val (q,n) = (xml \ "queue_name").text.trim match {
        case QueueInstance(q,n) ⇒ (q,n)
        case _                  ⇒ ("","")
      }

      Job (
        id = (xml \ "JB_job_number").text.toInt,
        priority = (xml \ "JAT_prio").text.toDouble,
        name = (xml \ "JB_name").text,
        owner = (xml \ "JB_owner").text,
        state = (xml \ "state").text,
        start = (xml \ "JAT_start_time").text,
        queue = q,
        node = n,
        slots = (xml \ "slots").text.toInt
      )
    }
  }

}
