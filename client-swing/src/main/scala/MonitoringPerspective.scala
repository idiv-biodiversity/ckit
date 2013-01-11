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

import java.awt.BorderLayout
import java.awt.event._

import java.beans._

import javax.swing._
import javax.swing.event._

/** Perspective for monitoring activities. */
object MonitoringPerspective extends BorderPanel
  with Perspective
  with ChangeListener
  with PropertyChangeListener {

  val REFRESH_TIMEOUT_DEF   = 30
  val REFRESH_TIMEOUT_MAX   = 60
  val REFRESH_TIMEOUT_MIN   = 5
  val REFRESH_QUEUE_TIMEOUT = 60000

  /** Contains the job tables. */
  private val tabbedTables     = new TabbedPane with RefreshableTabbedPane
  tabbedTables.tabLayoutPolicy = TabbedPane.Layout.Scroll

  /** Contains the job details. */
  private val tabbedDetails     = new TabbedPane() with RefreshableTabbedPane
  tabbedDetails.tabLayoutPolicy = TabbedPane.Layout.Scroll

  /** Contains the chart. */
  private val chartPanel = new BorderPanel()

  /** Splits the lower part in left (chart) and right (job detail) part. */
  private val splitBottom        = new SplitPane(Orientation.Vertical, chartPanel, tabbedDetails)
  splitBottom.continuousLayout   = true
  splitBottom.oneTouchExpandable = true
  splitBottom.resizeWeight       = 0.4

  /** Splits the main view in upper (job table) and lower part. */
  private val splitMain        = new SplitPane(Orientation.Horizontal, tabbedTables, splitBottom)
  splitMain.continuousLayout   = true
  splitMain.oneTouchExpandable = true
  splitMain.resizeWeight       = 0.7

  /** The current filter. */
  private var filter: Option[TextField] = None

  private val filterLabel = new Label("filter: ")
  filterLabel.tooltip = "filter for the current job table"

  override val toolBar = new ToolBar()

  this.peer.add(toolBar,        BorderLayout.PAGE_START)
  this.peer.add(splitMain.peer, BorderLayout.CENTER)

  // -----------------------------------------------------------------------
  // listeners / actions
  // -----------------------------------------------------------------------

  tabbedTables.peer.addChangeListener(this)

  action.binding.CloseSelectedTab(tabbedDetails).applyDefaultBindings()
  action.binding.CloseSelectedTab(tabbedTables).applyDefaultBindings()
  action.binding.RefreshSelectedTab(tabbedDetails).applyDefaultBindings()
  action.binding.RefreshSelectedTab(tabbedTables).applyDefaultBindings()

  // -----------------------------------------------------------------------
  // end of constructor body / start of defs
  // -----------------------------------------------------------------------

  /** Adds a new job detail. */
  def addJobDetail(pane: JobPane, select: Boolean) {
    val page = new TabbedPane.Page(pane.name, pane)
    tabbedDetails.pages += page
    tabbedDetails.peer.setTabComponentAt(tabbedDetails.pages.length - 1,
      new TabTitle(tabbedDetails, pane).peer)

    if (select)
      tabbedDetails.selection.page = page
  }

  /** Adds a new job table. */
  def addJobTables(jobTable: JobListPane) {
    val page = new TabbedPane.Page(jobTable.name, jobTable)
    tabbedTables.pages += page
    tabbedTables.peer.setTabComponentAt(tabbedTables.pages.length - 1,
      new TabTitle(tabbedTables, jobTable).peer)

    // add tableModel listener to update accordingly
    jobTable.peer.addPropertyChangeListener("model", this)
    jobTable.table.peer.getSelectionModel.addListSelectionListener(StatusBar.createListSelectionListener(jobTable))

    tabbedTables.selection.page = page
  }

  /** Returns true if given job table is the selected one. */
  def isSelected(jobTable: JobListPane) = jobTable == tabbedTables.selection.page.content

  override def propertyChange(e: PropertyChangeEvent) = e.getPropertyName match {
    case "model"  ⇒ stateChanged(new ChangeEvent(this))
    case property ⇒ //StatusBar.publish("unwanted property @ MonitoringPerspective: " + property, true)
  }

  override def stateChanged(ce: ChangeEvent) {
    // first of all remove filter and chart
    chartPanel.peer.removeAll()
    filter.foreach { filter ⇒
      toolBar.remove(filterLabel.peer)
      toolBar.remove(filter.peer)
    }

    tabbedTables.pages.length match {
      case i if i < 1 ⇒ {
        StatusBar.setSelectedJobs(0)
      }
      case _ ⇒ {
        // there are tabs --> update components
        val jobTable = tabbedTables.selection.page.content.asInstanceOf[JobListPane]
        val filter = jobTable.filterField

        // update filter
        toolBar.add(filterLabel.peer)
        this.filter = Some(filter)
        toolBar.add(filter.peer)
        filterLabel.peer.setLabelFor(filter.peer)

        // update selected jobs
        StatusBar.setSelectedJobs(jobTable.selectedRowCount)
      }
    }

    chartPanel.peer.repaint()
    toolBar.repaint()
  }

  // TODO apply filter for all tables checkbox
  class ToolBar extends JToolBar("MonitoringPerspectiveToolBar", SwingConstants.HORIZONTAL)
    with ItemListener {

    this.setRollover(true)
    this.setFloatable(false)

    /** Label for the timer showing the remaining time until a global refresh. */
    private var refreshRemain = new Label(REFRESH_TIMEOUT_DEF.toString)

    /** Timeout interval in seconds. */
    private var refreshTimeout = REFRESH_TIMEOUT_DEF

    /** Allows to set the timeout of the timer. */
    private val timeoutItems = scala.collection.mutable.ArrayBuffer[Int]()
    for ( i ← REFRESH_TIMEOUT_MIN to REFRESH_TIMEOUT_MAX ) {
      if ( i % 5 == 0 )
        timeoutItems += i
    }
    private val refreshTimeoutSetter    = new ComboBox(timeoutItems)
    refreshTimeoutSetter.selection.item = REFRESH_TIMEOUT_DEF
    refreshTimeoutSetter.maximumSize    = new Dimension(50, 20)
    refreshTimeoutSetter.preferredSize  = new Dimension(50, 20)
    refreshTimeoutSetter.peer.addItemListener(this)

    /** Swing Timer to refresh the components. */
    private val refreshTimer = new Timer(1000, new RefreshAllMonitorsAction())
    refreshTimer.setInitialDelay(0)

    /** Timer for refreshing the queue summaries. */
    private val queueRefreshTimer = new Timer(REFRESH_QUEUE_TIMEOUT, new RefreshQueueAction())
    queueRefreshTimer.setInitialDelay(500)

    /** A panel to show which queues are available. */
    private val queues = new QueueAvailability()

    onEDT {
      SwingClient.top.peer.addWindowListener(MainFrameWindowListener)
    }

    // -----------------------------------------------------------------------
    // adding
    // -----------------------------------------------------------------------

    addSeparator()
    add(new Label("refresh: ").peer)
    add(refreshTimeoutSetter.peer.asInstanceOf[java.awt.Component])
    add(new Label(" in ").peer)
    add(refreshRemain.peer)
    addSeparator()
    add(queues.peer)
    addSeparator()

    // -----------------------------------------------------------------------
    // defs
    // -----------------------------------------------------------------------

    override def itemStateChanged(e: ItemEvent) {
      refreshTimeout     = refreshTimeoutSetter.selection.item
      refreshRemain.text = refreshTimeout.toString
    }

    /** Starts all timers. */
    private def startTimers() {
      refreshTimer.start()
      queueRefreshTimer.start()
    }

    /** Stops all timers. */
    private def stopTimers() {
      refreshTimer.stop()
      queueRefreshTimer.stop()
    }

    object MainFrameWindowListener extends WindowAdapter {
      /** Starts all timers. */
      override def windowDeiconified(e: WindowEvent) {
        startTimers()
      }

      override def windowGainedFocus(e: WindowEvent) {
        swing.action.QueueSummary()
        tabbedTables.refreshAll()
        tabbedDetails.refreshAll()
      }

      /** Stops all timers. */
      override def windowIconified(e: WindowEvent) = stopTimers()
    }

    private class RefreshAllMonitorsAction extends ActionListener {
      override def actionPerformed(e: ActionEvent) {
        // just a helper
        val currentTime = refreshRemain.text.toInt

        if (currentTime > 1) {
          refreshRemain.text = (currentTime - 1).toString
        } else {
          refreshRemain.text = refreshTimeout.toString
          tabbedTables.refreshAll()
          tabbedDetails.refreshAll()
        }
      }
    }

    private class RefreshQueueAction extends ActionListener {
      override def actionPerformed(ae: ActionEvent) {
        swing.action.QueueSummary()
      }
    }
  }
}
