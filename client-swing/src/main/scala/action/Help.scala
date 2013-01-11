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

import scala.swing._

/** Shows help. */
object Help extends scala.swing.Action("Help") {
  mnemonic    = Key.H.id
  accelerator = Some(keyStroke(Key.F1))

  override def apply {
    // preparing the dialog
    val dialog = new Dialog(SwingClient.top) {
      override def closeOperation() = dispose()
    }
    dialog.title = "Help"
    dialog.modal = false
    dialog.preferredSize = new Dimension(640, 480)

    val editorPane = new EditorPane("text/html", message)
    editorPane.peer.setEditable(false)

    // preparing the display of the notifications
    val scrollPane = new ScrollPane
    scrollPane.contents = editorPane
    dialog.peer.add(scrollPane.peer)

    // finishing and showing the dialog
    editorPane.peer.setCaretPosition(0)
    dialog.peer.pack()
    dialog.visible = true
  }

  val message = """<html><div align="justify">
<h1>Getting started</h1>

Like you may have already experienced, the ClusterKit client starts with a login prompt.
Use your LDAP account - you will not need to register a separate account.
With successfully logging in the client instantly gets you an overview of your jobs on the cluster.
The view you get is divided into three main parts:
the upper one shows a table of jobs,
the lower left a pie chart of the current job-table and
the lower right is reserved for detailed information about jobs.
<p>
Tip: You can resize or hide these main parts by dragging the separator or clicking on the small triangles on the separator.
<p>

<h1>The components</h1>

An intention of the client is to be easy-to-use, intuitive and self-explanatory.
At some points, where this isn't directly possible, we tried to provide tooltips,
which you can get by hovering over the menu item, label, etc. with the mouse.
<p>
The job tables: You can get new job table views via the monitoring menu.
Sorting is realized by clicking on the table header and filtering using the filter text field in the toolbar.
Each table has its own filter.
By pressing ENTER on selected jobs or double-clicking on one you get a detailed job view of the current selection.
There is also a popup menu by right-clicking on a job, which features the same functionality as pressing ENTER.
Here you may later see additional functions, e.g. suspend/resume or kill a job.
<p>
Tip: Try right-clicking on tabs.
<p>
The tool bar: Here you can see a global refresh timer ticking.
You can modify its timeout and, if reached, all views will be updated, except for the ones you disabled.
The colored labels in the center of the toolbar represent the main queues of the cluster.
A queue will be red if either there are no available slots OR
the load threshold of the queue is reached (the queue has to much work to do, independently of the used slots).
So jobs waiting on this queue have to wait until there are available slots AND
the current load is below the load threshold -- which is displayed by a green queue in the toolbar.
<p>
The status bar: You may have already noticed, the changing numbers on the outer left part of the status bar.
You'll get what they are for by the tooltip provided when hovering with the mouse over them.
The main part is reserved for notifications.
Only the last one is displayed, but you can get the notification history by pressing the show button.
In some cases you can see there a little more debugging information.
<p>
Note: Since this project is still in its early stages,
you may want to contribute a little to its healthy future by reporting bugs and/or request new features.
For your convenience there is a shortcut in the Help menu for exactly that purpose.
Thanks in advance for any feedback!
<p>

<h1>Keyboard shortcuts</h1>

Some shortcuts you may have already seen in the menu, like pressing F1 to get to this help.
<p>
For any tab, which has the keyboard focus:
<dl>
<dt>CTRL + W<dd>closes it
<dt>CTRL + R or F5<dd>refreshes it
</dl>
Tip: Generally you can reach any underscored button / menu item / etc. of the current window by pressing ALT+underscored character.
<p>

<h1>Scheduled for implementation</h1>

<ol>
<li>suspend/resume jobs
<li>kill jobs
<li>submit jobs
</ol>
</div></html>"""
}
