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
import scala.swing.event.KeyPressed

import scala.collection.JavaConversions._

import scala.util.control.Exception._

import java.awt.event.{ MouseEvent, MouseAdapter }

import javax.swing._
import javax.swing.RowSorter.SortKey
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.event.ListSelectionListener
import javax.swing.plaf.basic.BasicTableHeaderUI
import javax.swing.table._

import java.beans._

object JobListPane {
  val columns = Seq("id", "priority", "name", "owner", "state", "start", "queue", "node", "slots")
  val columnClasses = Seq(classOf[Integer],classOf[java.lang.Double],classOf[String],classOf[String],classOf[State],classOf[String],classOf[String],classOf[String],classOf[Integer])

  class TModel(rowData: Array[Array[Object]]) extends AbstractTableModel {
    def this(jobs: Seq[Job]) = this(jobs.toArray map { j ⇒
      Array[Object](Int.box(j.id), Double.box(j.priority), j.name, j.owner, j.state, j.start, j.queue, j.node, Int.box(j.slots))
    })

    override def getColumnName(column: Int) = columns(column).toString
    override def getRowCount() = rowData.length
    override def getColumnCount() = columns.length
    override def getColumnClass(col: Int) = columnClasses(col)
    override def getValueAt(row: Int, col: Int): Object = rowData(row)(col)
    override def isCellEditable(row: Int, col: Int) = false
    override def setValueAt(value: Any, row: Int, col: Int) {}
  }
}

class JobListPane(val username: Option[String], private var jobs: Seq[Job]) extends ScrollPane with PopupMenuTriggerable with Refreshable {

  object filter extends TextField with DocumentListener {
    peer.getDocument.addDocumentListener(this)

    override def changedUpdate(e: DocumentEvent) = apply
    override def  insertUpdate(e: DocumentEvent) = apply
    override def  removeUpdate(e: DocumentEvent) = apply

    def apply = ignoring(classOf[java.util.regex.PatternSyntaxException]) {
      table.peer.getRowSorter match {
        case sorter: DefaultRowSorter[_,_] ⇒
          sorter.setRowFilter(RowFilter.regexFilter(text))
        case _ ⇒
      }
    }
  }

  val table = new Table()
  table.model = new JobListPane.TModel(jobs)
  table.peer.addPropertyChangeListener(propertyChangeListener)
  table.peer.addPropertyChangeListener(MonitoringPerspective)
  table.peer.addMouseListener(this)

  table.selection.elementMode  = Table.ElementMode.Row
  table.selection.intervalMode = Table.IntervalMode.MultiInterval
  table.peer.setDefaultRenderer(classOf[Object], StateCategoryRenderer)
  table.peer.setDefaultRenderer(classOf[Number], StateCategoryRenderer)
  table.peer.setDefaultRenderer(classOf[java.lang.Double], StateCategoryRenderer)

  table.peer.setRowSorter(new TableRowSorter(table.model))

  // replace the old mouse input handler with the one from this class
  private val header = table.peer.getTableHeader
  for (m ← header.getMouseListeners) {
    if (m.isInstanceOf[BasicTableHeaderUI#MouseInputHandler]) {
      val mih = m.asInstanceOf[BasicTableHeaderUI#MouseInputHandler]
      header.removeMouseListener(mih)
      header.addMouseListener(new MouseInputHandler(mih))
    }
  }

  // -----------------------------------------------------------------------
  // bindings
  // -----------------------------------------------------------------------

  listenTo(filter.keys, table.keys)
  reactions += {
    case event @ KeyPressed(`table`, Key.Slash, _, _) ⇒
      StatusBar.add(filter.peer)
      StatusBar.revalidate
      filter.requestFocus()

    case event @ KeyPressed(`filter`, key, _, _) if key == Key.Enter || key == Key.Escape ⇒
      StatusBar.remove(filter.peer)
      StatusBar.revalidate
  }

  private val im = table.peer.getInputMap(action.binding.ancestorOfFocusedComponent)

  // let (shift) tab do what down (up) does
  im.put(keyStroke(Key.Tab), im.get(keyStroke(Key.Down)))
  im.put(keyStroke(Key.Tab, Modifier.Shift), im.get(keyStroke(Key.Up)))

  // let home and end do what ctrl home and end does
  im.put(keyStroke(Key.Home), im.get(keyStroke(Key.Home, Modifier.Control)))
  im.put(keyStroke(Key.End),  im.get(keyStroke(Key.End,  Modifier.Control)))

  // apply a new enter action to the table
  private val dfs = action.binding.DetailForSelection(table)
  dfs.applyDefaultBindings()

  /** The popup menu. */
  private val popupMenu = new JPopupMenu("For all selected jobs ...")
  popupMenu.add(new Label(popupMenu.getLabel).peer)
  popupMenu.addSeparator()
  popupMenu.add(new JMenuItem(dfs.peer))

  this.viewportView = table

  // -----------------------------------------------------------------------
  // defs
  // -----------------------------------------------------------------------

  def update(jobs: Seq[Job]) {
    val newModel = new JobListPane.TModel(jobs)
    val oldModel = table.model

    if (newModel == oldModel)
      return

    val oldSortKeys  = table.peer.getRowSorter.getSortKeys
    val oldColums    = table.peer.getColumnModel.getColumns.toArray
    val oldSelection = table.selection.rows map { row ⇒
      (row, table(row, 0).asInstanceOf[Int]) // pair of row and jobID
    }

    table.model = newModel
    val newSorter = new TableRowSorter(table.model)
    table.peer.setRowSorter(newSorter)
    newSorter.setSortKeys(oldSortKeys)

    filter.apply

    val rowCount = table.model.getRowCount
    oldSelection.foreach { pair ⇒
      val row = pair._1
      val id  = pair._2

      if (row < rowCount && table(row, 0).asInstanceOf[Int] == id)
        table.selection.rows += row
      else
        for (i ← 0 until rowCount)
          if (table(i,0).asInstanceOf[Int] == id)
            table.selection.rows += i
    }

    // column model
    val colModel = new DefaultTableColumnModel()
    for (col ← oldColums)
      colModel.addColumn(col)
    table.peer.setColumnModel(colModel)
  }

  private object propertyChangeListener extends PropertyChangeListener {
    def propertyChange(e: PropertyChangeEvent) = e.getPropertyName match {
      case "model" ⇒
        val oldModel = e.getOldValue
        val newModel = e.getNewValue

        JobListPane.this.name = newModel.toString

      case "rowSorter" ⇒
        val oldValue = e.getOldValue
        val newValue = e.getNewValue

        newValue match {
          case newSorter: DefaultRowSorter[_,_] ⇒
            newSorter.setComparator(4, State.ordering)

            oldValue match {
              case null ⇒
                val sortKeys = new java.util.ArrayList[SortKey]()
                sortKeys.add(new SortKey(3, SortOrder.ASCENDING))
                sortKeys.add(new SortKey(4, SortOrder.ASCENDING))
                newSorter.setSortKeys(sortKeys)

              case oldSorter: RowSorter[_] ⇒/*
                println("old")
                oldSorter.getSortKeys foreach println
                newSorter.setSortKeys(oldSorter.getSortKeys)
                println("new")
                newSorter.getSortKeys foreach println
                println(oldSorter)
                println(newSorter)
                println(table.peer.getRowSorter)*/

              case _ ⇒
            }

          case _ ⇒
        }

      case "columnModel" ⇒
        val oldModel = e.getOldValue
        val newModel = e.getNewValue

      case property ⇒
        // StatusBar.publish("unwanted property @ JobTable: " + property, true)
    }
  }

  /** Returns the number of selected rows (jobs). */
  def selectedRowCount = table.selection.rows.size

  override def mouseClicked(me: MouseEvent) {
    if (SwingUtilities.isLeftMouseButton(me) && me.getClickCount >= 2) {
      action.binding.DetailForSelection(table)()
    }
  }

  override def mousePressed(me: MouseEvent) {
    showPopupMenu(me)
  }

  override def mouseReleased(me: MouseEvent) {
    showPopupMenu(me)
  }

  override def refresh() {
    if (refreshEnabled) {
      username match {
        case Some(user) ⇒ action.JobListFor(user)
        case None       ⇒ action.JobList()
      }
    }
  }

  override def showPopupMenu(me: MouseEvent) {
    if (me.isPopupTrigger) {
      popupMenu.show(me.getComponent, me.getX, me.getY)
    }
  }

  /**
   * Advanced mouse listener for the {@link javax.swing.table.JTableHeader}. It
   * allows the user to apply multiple {@link javax.swing.RowSorter.SortKey}s.
   * <p>
   * The old {@link javax.swing.plaf.basic.BasicTableHeaderUI.MouseInputHandler}
   * will be kept to support the old functions.
   */
  private class MouseInputHandler(oldMIH: BasicTableHeaderUI#MouseInputHandler) extends MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      // left clicks mod 2 == 1
      if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount % 2 == 1) {
        val oldSortKeys = table.peer.getRowSorter.getSortKeys
        val colIndex = table.viewToModelColumn(table.peer.columnAtPoint(e.getPoint))

        if (oldSortKeys.isEmpty) {
          // ================================================================
          // =========================empty old list=========================
          // ================================================================
          // --> add ascending sort key
          setSingleKey(colIndex, SortOrder.ASCENDING)

          // ================================================================
          // ==============from here on the list is NOT empty================
          // ================================================================

        } else if (e.isControlDown) {
          // ================================================================
          // ========================CTRL was pressed========================
          // ================================================================
          // --> if sort key already present swap it otherwise add ascending

          // copy immutable list to new list
          var newSortKeys = new java.util.ArrayList[SortKey]()
          for (sortKey ← oldSortKeys)
            newSortKeys.add(sortKey)

          // check if col is in key list ...
          for (i ← 0 until newSortKeys.size) {
            val key = newSortKeys.get(i)
            if (colIndex == key.getColumn) {
              // ... yep -- set new order depending on old and return
              val newOrder = newSortOrder(key)

              // if unsorted just remove the key otherwise replace the old
              if (newOrder == SortOrder.UNSORTED) {
                newSortKeys.remove(i)
              } else {
                newSortKeys.set(i, new SortKey(colIndex, newOrder))
              }

              // update sorter and return
              table.peer.getRowSorter.setSortKeys(newSortKeys)
              return
            }
          }

          // ... key was not found in list: add new ascending and return
          newSortKeys.add(new SortKey(colIndex, SortOrder.ASCENDING))
          table.peer.getRowSorter.setSortKeys(newSortKeys)
          return

        } else {
          // ================================================================
          // ======================CTRL was NOT pressed======================
          // ================================================================
          // --> if key present swap otherwise new ascending

          // check if col is in key list ...
          for (key ← oldSortKeys) {
            if (colIndex == key.getColumn) {
              // ... yep -- set new order depending on old and return
              val newOrder = newSortOrder(key)

              // if unsorted null as empty list
              if (newOrder != SortOrder.UNSORTED) {
                setSingleKey(colIndex, newOrder)
              } else {
                table.peer.getRowSorter.setSortKeys(null)
              }
              return
            }
          }

          // ... key was not found in list: new ascending and return
          setSingleKey(colIndex, SortOrder.ASCENDING)
          return
        }
      }
    }

    override def mouseDragged(e: MouseEvent)  = oldMIH.mouseDragged(e)
    override def mouseEntered(e: MouseEvent)  = oldMIH.mouseEntered(e)
    override def mouseExited(e: MouseEvent)   = oldMIH.mouseExited(e)
    override def mouseMoved(e: MouseEvent)    = oldMIH.mouseMoved(e)
    override def mousePressed(e: MouseEvent)  = oldMIH.mousePressed(e)
    override def mouseReleased(e: MouseEvent) = oldMIH.mouseReleased(e)

    /**
     * Gets the following sort order from the order of the old sort key.
     * <p>
     * <ul>
     * <li>ASCENDING --> DESCENDING</li>
     * <li>DESCENDING --> UNSORTED</li>
     * <li>UNSORTED --> ASCENDING</li>
     * </ul>
     */
    private def newSortOrder(sortKey: SortKey): SortOrder = sortKey.getSortOrder match {
      case SortOrder.ASCENDING ⇒ SortOrder.DESCENDING
      case SortOrder.UNSORTED  ⇒ SortOrder.ASCENDING
      case _                   ⇒ SortOrder.UNSORTED
    }

    /**
     * Applies a single sort key to the sorter.
     * 
     * @param colIndex
     *          the column index
     * @param newSortOrder
     *          the new sort order the key will get
     */
    private def setSingleKey(colIndex: Int, newSortOrder: SortOrder) {
      table.peer.getRowSorter.setSortKeys(List(new SortKey(colIndex, newSortOrder)))
    }
  }
}
