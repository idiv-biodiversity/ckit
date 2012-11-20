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
package daemon

import akka.actor.Actor

import scala.sys.process._
import scala.xml.XML

class GridEngineActor extends Actor {
  def receive = {
    case Protocol.JobList ⇒ for {
      jobs ← GridEngine.jobList(XML.loadString("qstat -xml".!!))
    } sender ! JobList(jobs)

    case Protocol.JobDetail(id: Int) ⇒ for {
      detail ← GridEngine.jobDetail(XML.loadString("qstat -xml -j %d".format(id).!!))
    } sender ! detail

    case Protocol.QueueSummary ⇒ for {
      summary ← GridEngine.queueSummary(XML.loadString("qstat -xml -g c".!!))
    } sender ! QueueSummaryList(summary)

    case Protocol.RuntimeSchedule ⇒ for {
      schedule ← GridEngine.runtimeSchedule(XML.loadString("qstat -xml -r".!!))
    } sender ! RuntimeSchedule(schedule)
  }
}
