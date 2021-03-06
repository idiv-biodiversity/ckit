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

import sys.process._
import util._
import xml.XML

import akka.actor.Actor
import akka.event.Logging

class GridEngineActor extends Actor with GridEngine {
  val log = Logging(context.system, this)

  def receive = {
    case Protocol.JobList => jobList match {
      case     Success(jobs)   => sender ! JobList(jobs)
      case f @ Failure(reason) => handleFailure(f, reason, Protocol.JobList)
    }

    case msg @ Protocol.JobListFor(users) => jobList(users) match {
      case     Success(jobs)   => sender ! JobList(jobs)
      case f @ Failure(reason) => handleFailure(f, reason, msg)
    }

    case msg @ Protocol.JobDetail(id) => jobDetail(id) match {
      case     Success(detail) => sender ! detail
      case f @ Failure(reason) => handleFailure(f, reason, msg)
    }

    case Protocol.QueueSummary => queueSummary match {
      case     Success(summary) => sender ! QueueSummaryList(summary)
      case f @ Failure(reason)  => handleFailure(f, reason, Protocol.QueueSummary)
    }

    case Protocol.RuntimeSchedule => runtimeSchedule match {
      case     Success(schedule) => sender ! schedule
      case f @ Failure(reason)   => handleFailure(f, reason, Protocol.RuntimeSchedule)
    }

    case msg @ Protocol.NodeInfo(node) => nodeInfo(node) match {
      case     Success(nodeinfo) => sender ! nodeinfo
      case f @ Failure(reason)   => handleFailure(f, reason, msg)
    }

    case Protocol.NodeInfoList => exechosts match {
      case     Success(nodes)  => sender ! ListNodeInfo(nodes.par.flatMap(node => nodeInfo(node).toOption.toList).toList)
      case f @ Failure(reason) => handleFailure(f, reason, Protocol.NodeInfoList)
    }

    case Protocol.ExecHosts => exechosts match {
      case     Success(nodes)  => sender ! NodeList(nodes)
      case f @ Failure(reason) => handleFailure(f, reason, Protocol.ExecHosts)
    }
  }

  def handleFailure(failure: Failure[_], reason: Throwable, message: Any) {
    log.error(reason, "Failure due to [{}] when processing [{}].", reason.getMessage, message)
    sender ! failure
  }
}
