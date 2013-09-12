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

import action._

import java.awt.event._

import java.beans._

import javax.swing.JMenuItem
import javax.swing.JPopupMenu

/**
 * Instances of this class are used as custom tab-title-components for
 * {@link javax.swing.JTabbedPane}s.
 */
// TODO this is refreshable specific - change it to support other tabs
class TabTitle(
    val tabbedPane: TabbedPane with RefreshableTabbedPane,
    comp: Component with Refreshable)
  extends FlowPanel(FlowPanel.Alignment.Center)()
  with ItemListener
  with PopupMenuTriggerable
  with PropertyChangeListener {

  hGap = 0
  vGap = 0
  opaque = false

  comp.refreshProperty.addPropertyChangeListener(this)

  comp.peer.addPropertyChangeListener("name", this)

  val titleText = {
    var s = comp.name
    if (s == null) "" else s
  }
  this.name = titleText

  private val closeTab = new CloseTab(this)

  // -----------------------------------------------------------------------
  // popup menu
  // -----------------------------------------------------------------------

  private val popupMenu = new JPopupMenu()

  private val refreshEnabledCheckBox = new CheckMenuItem("Refresh enabled?")
  refreshEnabledCheckBox.mnemonic = Key.D
  refreshEnabledCheckBox.peer.setState(true)
  refreshEnabledCheckBox.peer.addItemListener(this)

  popupMenu.add(new JMenuItem(new RefreshTab(this).peer))
  popupMenu.add(new JMenuItem( RefreshAllTabs(tabbedPane).peer))
  popupMenu.add(refreshEnabledCheckBox.peer)
  popupMenu.addSeparator()
  popupMenu.add(new JMenuItem( new CloseOtherTabs(this).peer ))
  popupMenu.add(new JMenuItem( CloseAllTabs(tabbedPane).peer))
  popupMenu.add(new JMenuItem(closeTab.peer))

  // -----------------------------------------------------------------------
  // adding
  // -----------------------------------------------------------------------

  private val titleLabel = new Label(titleText)
  titleLabel.peer.addMouseListener(this)

  peer.add(titleLabel.peer)
  peer.add(new CloseButton(closeTab).peer)

  // -----------------------------------------------------------------------
  // defs
  // -----------------------------------------------------------------------

  override def itemStateChanged(e: ItemEvent) {
    comp.refreshEnabled = (e.getStateChange == ItemEvent.SELECTED)
  }

  override def mouseClicked(me: MouseEvent) {
    // select this tab
    tabbedPane.selection.index = tabbedPane.peer.indexOfTabComponent(this.peer)
  }

  override def mousePressed(me: MouseEvent) {
    showPopupMenu(me)
  }

  override def mouseReleased(me: MouseEvent) {
    showPopupMenu(me)
  }

  override def propertyChange(e: PropertyChangeEvent) {
    if (e.getPropertyName.equals(Refreshable.propertyName) &&
        e.getNewValue.isInstanceOf[Boolean]) {
      refreshEnabledCheckBox.peer.setSelected(e.getNewValue.asInstanceOf[Boolean])
    } else if (e.getPropertyName.equals("name")) {
      titleLabel.text = e.getNewValue.toString
    }
  }

  /** Shows the menu if MouseEvent was trigger. */
  protected def showPopupMenu(me: MouseEvent) {
    if (me.isPopupTrigger) {
      popupMenu.show(me.getComponent, me.getX, me.getY)
    }
  }
}
