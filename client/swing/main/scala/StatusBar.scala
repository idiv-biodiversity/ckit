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

import action._

import scala.swing._

import javax.swing.JToolBar
import javax.swing.SwingConstants
import javax.swing.text.PlainDocument

/** Status bar for displaying selections, requests, notifications. */
object StatusBar extends JToolBar("StatusBar", SwingConstants.HORIZONTAL) {

  setFloatable(false)
  setRollover(true)
  setPreferredSize(new Dimension(800, 23))

  /** Document containing the notifications. */
  private val document = new PlainDocument()

  /** Label to display a short version of the latest notification. */
  private val messageLabel = new Label()

  // -----------------------------------------------------------------------
  // adding
  // -----------------------------------------------------------------------

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

  def notifications = document

}
