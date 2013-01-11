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
package binding

import scala.swing.TabbedPane

object RefreshSelectedTab extends Multiton[TabbedPane with RefreshableTabbedPane, RefreshSelectedTab] {
  override val create = (tabbedPane: TabbedPane with RefreshableTabbedPane) ⇒
    new RefreshSelectedTab(tabbedPane)
}

class RefreshSelectedTab(val comp: TabbedPane with RefreshableTabbedPane) extends Action("")
  with Binding[TabbedPane with RefreshableTabbedPane] {

  mnemonic = Key.T.id

  /** Refreshes the selected tab. */
  override def apply {
    val index = comp.selection.index
    if (index >= 0) {
      comp.refresh(index)
    }
  }

  override def applyDefaultBindings() {
    // bindings
    comp.peer.getInputMap(ancestorOfFocusedComponent).put(keyStroke(Key.F5), toString)
    comp.peer.getInputMap(ancestorOfFocusedComponent).put(keyStroke(Key.R, Modifier.Control), toString)

    // action
    comp.peer.getActionMap.put(toString, this.peer)
  }
}
