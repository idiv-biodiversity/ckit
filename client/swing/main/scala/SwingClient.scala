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
import scala.swing.event._
import scala.util._

import akka.actor.{ ActorSystem, Props }

object SwingClient extends SwingApplication {
  val system = ActorSystem("ckit")
  val remote = system.actorFor("akka.tcp://ckit@141.65.122.14:2552/user/grid-engine-actor")
  val proxy = system.actorOf(Props[Proxy], name = "proxy")

  lazy val menuBar: MenuBar = {
    val bar = new MenuBar

    val monitoring = new Menu("Monitoring")
    monitoring.contents += new MenuItem(action.JobDetail)
    monitoring.contents += new MenuItem(action.JobList)
    monitoring.contents += new MenuItem(action.JobListFor)
    monitoring.contents += new MenuItem(action.QueueSummary)
    monitoring.contents += new MenuItem(action.RuntimeSchedule)

    val main = new Menu("Main")
    main.contents += monitoring
    main.contents += new Separator
    main.contents += new MenuItem(action.Quit)

    val help = new Menu("Help")
    help.contents += new MenuItem(action.Help)
    help.contents += new MenuItem(action.Mail)
    help.contents += new Separator
    help.contents += new MenuItem(action.About)

    bar.contents += main
    bar.contents += help
    bar
  }

  lazy val top = new MainFrame {
    override def closeOperation() {
      SwingClient.quit()
    }
  }

  lazy val tabbed = new TabbedPane

  def startup(args: Array[String]) {
    top.title = "ClusterKit"
    top.menuBar = menuBar

    val panel = new BorderPanel
    panel.layout(tabbed) = BorderPanel.Position.Center
    panel.peer.add(StatusBar, java.awt.BorderLayout.SOUTH)

    top.contents = panel

    tabbed.listenTo(tabbed.keys)
    tabbed.reactions += {
      case event @ KeyPressed(_, key, modifiers, _)
        if modifiers == Key.Modifier.Control && key == Key.W ⇒
          tabbed.pages.remove(tabbed.selection.index)
    }

    tabbed.pages += new TabbedPane.Page("Welcome", new Label("... this is ClusterKit"))

    top.pack()
    top.visible = true
  }

  override def quit() {
    system.shutdown()
    sys.exit(0)
  }
}
