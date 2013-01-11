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

/** Mixin for [[TabbedPane]]s to refresh [[Refreshable]]s. */
trait RefreshableTabbedPane {
  self: TabbedPane ⇒

  /** Refreshes the [[Refreshable]] if contained by this. */
  def refresh(refreshable: Component with Refreshable) {
    val index = peer.indexOfComponent(refreshable.peer)

    // -1 indicates the component is not in this container
    if (index != -1) refreshable.refresh()
  }

  /** Refreshes the component at given index. */
  def refresh(index: Int) = pages(index).content match {
    case r: Refreshable ⇒ r.refresh()
    case _ ⇒
  }

  /** Refreshes all tab components. */
  def refreshAll() {
    for (index ← 0 until pages.length)
      refresh(index)
  }
}
