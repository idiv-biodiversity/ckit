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

import java.awt.Color

object StateCategory {
  implicit val ordering: Ordering[StateCategory] = new Ordering[StateCategory] {
    override def compare(a: StateCategory, b: StateCategory): Int = {
      a.id.compareTo(b.id)
    }
  }
}

sealed abstract class StateCategory {
  def id: Int
  def color: Color
  def light: Color
}

case object Running extends StateCategory {
  val id = 0
  val color = Color.GREEN
  val light = new Color(200, 255, 200)
}

case object Suspended extends StateCategory {
  val id = 1
  val color = Color.YELLOW
  val light = new Color(255, 255, 200)
}

case object Pending extends StateCategory {
  val id = 2
  val color = Color.BLUE
  val light = new Color(200, 200, 255)
}

case object Deleted extends StateCategory {
  val id = 3
  val color = Color.ORANGE
  val light = new Color(255, 225, 140)
}

case object Error extends StateCategory {
  val id = 4
  val color = Color.RED
  val light = new Color(255, 200, 200)
}

case object Unknown extends StateCategory {
  val id = 5
  val color = Color.GRAY
  val light = new Color(200, 200, 200)
}
