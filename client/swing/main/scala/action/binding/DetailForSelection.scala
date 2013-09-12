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

import scala.swing.Table

object DetailForSelection extends Multiton[Table,DetailForSelection] {
  val create = (table: Table) ⇒ new DetailForSelection(table)
}

class DetailForSelection(val comp: Table) extends Action("get detailed information") with Binding[Table] {
  mnemonic = Key.D.id

  override def apply = {
    val model     = comp.model
    val selection = comp.selection.rows.toArray

    // replace row index with jobID
    for (i ← 0 until selection.size)
      selection(i) = model.getValueAt(comp.peer.convertRowIndexToModel(selection(i)), 0).asInstanceOf[Int]

    JobDetail(selection)
  }

  override def applyDefaultBindings() {
    comp.peer.getInputMap(ancestorOfFocusedComponent).put(keyStroke(Key.Enter), toString)
    comp.peer.getActionMap.put(toString, this.peer)
  }
}
