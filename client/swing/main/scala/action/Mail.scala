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
package action

import ckit._

import java.net._
import java.awt.Desktop
import java.io.IOException

object Mail extends Action("Contact / Request Feature") {
  mnemonic = Key.R.id
  toolTip = "mail developer for feedback and requesting new features"

  val mailClientErrorMsg = "Either you have no default mail client or it failed to be launched."
  val mailUnsupportedMsg = "Your environment does not support mailing from within this application."

  /** Opens standard mail client. */
  override def apply = if (Desktop.isDesktopSupported) {
    val desktop = Desktop.getDesktop
    if (desktop.isSupported(Desktop.Action.MAIL)) {
      try {
        Desktop.getDesktop.mail(new URI("mailto:christian.krause@ufz.de?subject=ClusterKit"))
      } catch {
        case e: IOException ⇒ StatusBar.publish(mailClientErrorMsg)
        case e: Exception   ⇒ StatusBar.publish(e.toString)
      }
    } else
      StatusBar.publish(mailUnsupportedMsg)
  } else
    StatusBar.publish(mailUnsupportedMsg)
}
