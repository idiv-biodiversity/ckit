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

import java.awt.Color

object Job {
  lazy val DeletedRE = "d.*".r
  lazy val ErrorRE = "E.*".r
  lazy val PendingRE = "[^E]*qw".r
  lazy val RunningRE = "[^dE]*[rt]".r
  lazy val SuspendedRE = "[^dE]*[sST]".r

  sealed abstract class StateCategory(val color: Color, val light: Color)

  case object Running   extends StateCategory(Color.GREEN,  new Color(200, 255, 200))
  case object Suspended extends StateCategory(Color.YELLOW, new Color(255, 255, 200))
  case object Pending   extends StateCategory(Color.BLUE,   new Color(200, 200, 255))
  case object Deleted   extends StateCategory(Color.ORANGE, new Color(255, 225, 140))
  case object Error     extends StateCategory(Color.RED,    new Color(255, 200, 200))
  case object Unknown   extends StateCategory(Color.GRAY,   new Color(200, 200, 200))

  case class State(name: String, category: StateCategory)
}

case class Job (
    id: Int,
    priority: Double,
    name: String,
    owner: String,
    state: String,
    start: String,
    queue: String,
    node: String,
    slots: Int,
    requests: Map[String,String]
  )

case class JobList(jobs: Seq[Job])

case class JobDetail (
    name: String,
    id: String,
    owner: String,
    group: String,
    project: String,
    account: String,
    requests: Map[String,String],
    tasks: Seq[Task],
    messages: Seq[String],
    globalMessages: Seq[String]
  )

case class Task(id: Int, usage: Map[String,String])

case class ScheduleTask(node: String, id: Int, name: String, start: String, runtime: Long)

case class RuntimeSchedule(jobs: Seq[ScheduleTask])
