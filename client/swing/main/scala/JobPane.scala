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
import scala.swing.Swing._

class JobPane(private var data: JobDetail) extends ScrollPane with Refreshable {

  peer.getVerticalScrollBar.setUnitIncrement(10)
  peer.getHorizontalScrollBar.setUnitIncrement(10)

  private val pane = new BoxPanel(Orientation.Vertical)
  update(data)

  this.contents = pane

  listenTo(keys)
  reactions += {
    case event @ KeyPressed(source, Key.Left, Modifier.Alt, _) if source == JobPane.this ⇒
      SwingClient.view.back()
  }

  override def refresh() = if (refreshEnabled) {
    action.JobDetail(data.id)
  }

  def VerticalLabel(s: String): Label = {
    val label = new Label(s)
    label.border = EmptyBorder(0, 10, 0, 10)
    label.horizontalAlignment = Alignment.Left
    label
  }

  def update(data: JobDetail) {
    this.data = data
    pane.contents.clear()

    val general = new FlowPanel(FlowPanel.Alignment.Left)()
    general.hGap = 10
    general.vGap = 0
    general.border = TitledBorder(EtchedBorder(Lowered), "General")
    general.contents += new Label(s"Name: ${data.name}")
    general.contents += new Label(s"ID: ${data.id}")
    general.contents += new Label(s"Owner: ${data.owner}")
    general.contents += new Label(s"Group: ${data.group}")
    general.contents += new Label(s"Project: ${data.project}")
    general.contents += new Label(s"Account: ${data.account}")

    pane.contents += general

    if (data.requests.nonEmpty) {
      val panel = new GridPanel(1,data.requests.size)
      panel.hGap = 10
      panel.vGap = 0
      panel.border = TitledBorder(EtchedBorder(Lowered), "Resource Requests")

      for ((name,value) ← data.requests)
        panel.contents += new Label(s"$name: $value")

      pane.contents += panel
    }

    if (data.tasks.nonEmpty) {
      val tasks = new GridPanel(data.tasks.size,1)
      tasks.hGap = 10
      tasks.vGap = 0
      tasks.border = TitledBorder(EtchedBorder(Lowered), "Tasks")

      for (task ← data.tasks) {
        val panel = new FlowPanel(FlowPanel.Alignment.Left)()
        panel.hGap = 10
        panel.vGap = 0
        panel.border = TitledBorder(EtchedBorder(Lowered), task.id.toString)

        for ((resource,value) ← task.usage)
          panel.contents += new Label(s"${resource}: ${value}")

        tasks.contents += panel
      }

      pane.contents += tasks
    }

    if (data.messages.nonEmpty) {
      val panel = new FlowPanel(FlowPanel.Alignment.Left)()
      panel.hGap = 10
      panel.vGap = 0
      panel.border = TitledBorder(EtchedBorder(Lowered), "Scheduler Messages")

      panel.contents += VerticalLabel(data.messages.mkString("<html>","<br>","</html>"))

      pane.contents += panel
    }

    if (data.globalMessages.nonEmpty) {
      val panel = new FlowPanel(FlowPanel.Alignment.Left)()
      panel.hGap = 10
      panel.vGap = 0
      panel.border = TitledBorder(EtchedBorder(Lowered), "Global Scheduler Messages")

      panel.contents += VerticalLabel(data.globalMessages.mkString("<html>","<br>","</html>"))

      pane.contents += panel
    }

    this.name = data.name
  }
}
