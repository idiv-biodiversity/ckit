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

import language.postfixOps

import scala.xml.XML

import org.specs2._

class GridEngineParserSpec extends Specification { def is =

  // -----------------------------------------------------------------------------------------------
  // fragments
  // -----------------------------------------------------------------------------------------------

  "Grid Engine Parser Specification"                                                               ^
                                                                                                  p^
  "Job List Parsing"                                                                               ^
    "invalid target yields empty collection"                          ! invalid                    ^
    "empty queue_info yields empty collection"                        ! empty                      ^
    "valid input yields non empty collection"                         ! nonEmpty                   ^
                                                                                                 end
  // -----------------------------------------------------------------------------------------------
  // tests
  // -----------------------------------------------------------------------------------------------

  def invalid = GridEngine.parseJobListFromXML (
    XML.load(getClass.getResource(""))
  ).isFailure must beTrue

  def empty = GridEngine.parseJobListFromXML (
    XML.load(getClass.getResource("/empty-qstat-job-list.xml"))
  ).get must have size 0

  def nonEmpty = GridEngine.parseJobListFromXML (
    XML.load(getClass.getResource("/qstat-job-list.xml"))
  ).get must have size 2

}
