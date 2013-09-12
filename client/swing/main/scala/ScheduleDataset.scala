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
package client
package swing

import com.github.nscala_time.time.Imports._
import org.jfree.data.general.AbstractDataset

class ScheduleDataset(nodes: Map[String,Int], running: Seq[ScheduleTask], reserved: Seq[ScheduleTask]) extends AbstractDataset {
  def apply(node: String): Seq[(Int,(Int,Interval))] = {
    (running ++ reserved) collect {
      case ScheduleTask(nodes,id,name,start,runtime) if nodes.contains(node) ⇒
        val s = start.toDateTime
        (id, (nodes(node), s to (s + runtime.toInt.millis)))
    } sortBy { case (id,(slots,interval)) ⇒
      interval.getStart.getMillis
    }
  }

  def nodeNames: Seq[String] = nodes.keys.toSeq.sorted
}
