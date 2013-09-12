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

import scala.swing._
import scala.swing.Swing._

import java.awt.Color

/** Small panel for toolbars to show availability of queues. */
class QueueAvailability extends BoxPanel(Orientation.Horizontal) {
  peer.setDoubleBuffered(true)
  opaque = false

  def refresh(summaries: Seq[QueueSummary]) {
    contents.clear()
    for (summary ← summaries) {
      // TODO use buttons for submission
      val label = new Label(summary.name)
      label.opaque = true
      label.tooltip = if (summary.available > 0) {
        "accepts jobs"
      } else {
        "is overloaded"
      }
      label.background = if (summary.available > 0) {
        Color.GREEN
      } else {
        Color.RED
      }

      contents += label
      contents += RigidBox((5, 0))
    }
  }
}
