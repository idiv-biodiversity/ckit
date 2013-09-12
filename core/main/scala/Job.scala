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

object State {
  implicit val ordering: Ordering[State] = new Ordering[State] {
    override def compare(a: State, b: State): Int = {
      a.category.id.compareTo(b.category.id)
    }
  }
}

case class State(name: String, category: StateCategory)

case class Job (
    id: Int,
    priority: Double,
    name: String,
    owner: String,
    state: State,
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

case class ComputeNode(name: String, slots: Int)

case class Cluster(nodes: Set[ComputeNode])

case class Task(id: Int, usage: Map[String,String])

case class ScheduleTask(nodes: Map[String,Int], id: Int, name: String, start: String, runtime: Long)

case class RuntimeSchedule(cluster: Cluster, jobs: Seq[ScheduleTask], reservations: Seq[ScheduleTask])
