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

import javax.swing.JTable
import javax.swing.table._

object StateCategoryRenderer extends DefaultTableCellRenderer {
  def categoryOf(model: TableModel, row: Int): StateCategory =
    model.getValueAt(row, 4).asInstanceOf[State].category

  override def getTableCellRendererComponent(table: JTable, value: AnyRef, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int) = {
    val category = categoryOf(table.getModel, table.convertRowIndexToModel(row))

    super.setForeground(java.awt.Color.BLACK)

    if (isSelected) {
      super.setBackground(category.color)
    } else {
      super.setBackground(category.light)
    }

    setFont(table.getFont)

    val value = table.getModel.getValueAt(table.convertRowIndexToModel(row),table.convertColumnIndexToModel(column)) match {
      case state: State ⇒ state.category
      case x ⇒ x
    }

    setValue(value)

    this
  }
}
