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

import java.awt.{ BasicStroke, Color, Graphics }

import javax.swing.JButton
import javax.swing.plaf.basic.BasicButtonUI

import scala.swing._

class CloseButton(action: Action) extends Button(action) {
  override lazy val peer: JButton = new JButton("") with SuperMixin {
    setPreferredSize(new Dimension(17, 17))
    setUI(new BasicButtonUI())
    setContentAreaFilled(false)
    setFocusable(false)
    setBorderPainted(false)
    setRolloverEnabled(true)

    override def paintComponent(g: Graphics) {
      val g2 = g.create.asInstanceOf[Graphics2D]

      if (getModel.isPressed) {
        g2.translate(1, 1)
      }

      g2.setStroke(new BasicStroke(2))

      if (getModel.isRollover) {
        g2.setColor(Color.MAGENTA)
      } else {
        g2.setColor(Color.BLACK)
      }

      val delta = 6
      g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1)
      g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1)

      g2.dispose()
    }

    override def updateUI() {}
  }
}
