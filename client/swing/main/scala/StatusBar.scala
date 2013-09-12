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

import javax.swing.JToolBar
import javax.swing.SwingConstants
import javax.swing.SwingWorker
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener
import javax.swing.text.PlainDocument

import akka.actor._

import ckit.client._
import action._

/** Status bar for displaying selections, requests, notifications. */
object StatusBar extends JToolBar("StatusBar", SwingConstants.HORIZONTAL) {

  setFloatable(false)
  setRollover(true)
  setPreferredSize(new Dimension(800, 23))

  /** Document containing the notifications. */
  private val document = new PlainDocument()

  /** Label to display a short version of the latest notification. */
  private val messageLabel = new Label()

  /** Label to display how much jobs of the currently displayed table are selected. */
  private val selectedJobs = new Label("0")
  selectedJobs.tooltip = "... job/s is/are selected in the table"

  // -----------------------------------------------------------------------
  // adding
  // -----------------------------------------------------------------------

  add(selectedJobs.peer)
  addSeparator
  add(messageLabel.peer)
  addSeparator
  add(Swing.HGlue.peer)
  add(new Button(ShowNotifications).peer)
  add(new Button(ClearNotifications).peer)

  // -----------------------------------------------------------------------
  // logging related stuff
  // -----------------------------------------------------------------------

  import akka.actor.Actor
  import akka.event.Logging._

  class EventListener extends Actor {
    def receive = {
      case Error(cause,_,_,msg) ⇒ Swing.onEDT {
        publish(cause)
      }
      case Warning(_,_,msg) ⇒ Swing.onEDT {
        messageLabel.text = msg.toString
        publish(msg.toString)
      }
      case Info(_,_,msg) ⇒ Swing.onEDT {
        messageLabel.text = msg.toString
        publish(msg.toString)
      }
      case Debug(_,_,msg) ⇒ Swing.onEDT {
        messageLabel.text = msg.toString
        publish(msg.toString)
      }
      case event ⇒ Swing.onEDT {
        messageLabel.text = event.toString
        publish(event.toString)
      }
    }
  }

  def publish(msg: String) {
    document.insertString(document.getLength, "%s%n".format(msg), null)
  }

  def publish(error: Throwable) {
    Option(error.getMessage) filterNot { _.trim == "" } foreach { message ⇒
      messageLabel.text = message
    }
    publish(stackTraceFor(error))
  }

  // -----------------------------------------------------------------------
  // other
  // -----------------------------------------------------------------------

  def createListSelectionListener(jobTable: JobListPane): ListSelectionListener = new JobTableSelectionListener(jobTable)

  def notifications = document

  /** Updates the label displaying the amount of selected jobs in the currently displayed table. */
  def setSelectedJobs(s: Int) = selectedJobs.text = if (s > 0) {
    s.toString
  } else {
    "0"
  }

  class JobTableSelectionListener(jobTable: JobListPane) extends ListSelectionListener {
    // update if this is selected component
    override def valueChanged(e: ListSelectionEvent): Unit =
      if (MonitoringPerspective.isSelected(jobTable))
        setSelectedJobs(jobTable.selectedRowCount)
  }
}
