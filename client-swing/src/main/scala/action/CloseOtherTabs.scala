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

/** Closes all tabs of a tabbed pane except of the one of the tab title component. */
class CloseOtherTabs(tabTitle: TabTitle) extends Action("Close other tabs") {
  mnemonic      = Key.O.id
  // TODO mnemonicIndex = 6
  peer.putValue(javax.swing.Action.DISPLAYED_MNEMONIC_INDEX_KEY, 6)

  override def apply {
    val tabbedPane = tabTitle.tabbedPane
    val index = tabbedPane.peer.indexOfTabComponent(tabTitle.peer)

    if (index >= 0) {
      val tabCountBefore = tabbedPane.pages.length

      // remove all tabs after this one
      for (i ← index + 1 until tabCountBefore)
        tabbedPane.peer.removeTabAt(index + 1)

      // remove all tabs before this one
      for (i ← 0 until index)
        tabbedPane.peer.removeTabAt(0)
    }
  }
}
