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
import scala.swing.Swing._

import javax.swing._
import javax.swing.event._
import javax.swing.table._

/**
 * This class wraps around a changeable {@link javax.swing.table.TableRowSorter}
 * and uses its input text as a regular expression to apply updated
 * {@link javax.swing.RowFilter}s to the sorters
 * {@link javax.swing.table.TableModel}.
 * <p>
 * Changes on the underlying {@link javax.swing.text.Document} will immediately
 * update the filter.
 * <p>
 * WARNING: The filter will not be updated when the sorters model changes!
 */
class TableFilterField(jobTable: JobListPane) extends TextField with DocumentListener {

  peer.getDocument.addDocumentListener(this)

  override def changedUpdate(e: DocumentEvent) = updateFilter()
  override def  insertUpdate(e: DocumentEvent) = updateFilter()
  override def  removeUpdate(e: DocumentEvent) = updateFilter()

  /**
   * Will apply a new regular expression {@link javax.swing.RowFilter} to the
   * underlying {@link javax.swing.table.TableRowSorter}, if the expression in
   * this {@link javax.swing.JTextField} parses.
   */
  def updateFilter(): Unit = try {
    jobTable.table.peer.getRowSorter match {
      case sorter: DefaultRowSorter[_,_] ⇒ sorter.setRowFilter(RowFilter.regexFilter(text))
      case _ ⇒
    }
  } catch {
    case e: Exception ⇒
  }
}
