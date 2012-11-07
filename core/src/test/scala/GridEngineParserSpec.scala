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
    "invalid target yields empty collection"                          ! invalidList                ^
    "empty target yields empty collection"                            ! emptyList                  ^
    "valid input yields non empty collection"                         ! nonEmptyList               ^
                                                                                                  p^
  "Queue Summary Parsing"                                                                          ^
    "invalid target yields empty collection"                          ! invalidSummary             ^
    "valid input yields non empty collection"                         ! nonEmptySummary          ^t^
      "contains correct names of queues"                              ! nonEmptySummaryNames    ^bt^
                                                                                                  p^
  "Runtime Schedule Parsing"                                                                       ^
    "invalid target yields empty collection"                          ! invalidSchedule            ^
    "empty target yields empty collection"                            ! emptySchedule              ^
    "valid input yields non empty collection"                         ! nonEmptySchedule         ^t^
                                                                                                 end
  // -----------------------------------------------------------------------------------------------
  // tests
  // -----------------------------------------------------------------------------------------------

  def invalidList = GridEngine.jobList (
    XML.load(getClass.getResource(""))
  ).isFailure must beTrue

  def emptyList = GridEngine.jobList (
    XML.load(getClass.getResource("/qstat-job-list-empty.xml"))
  ).get must have size 0

  def nonEmptyList = GridEngine.jobList (
    XML.load(getClass.getResource("/qstat-job-list.xml"))
  ).get must have size 2

  def invalidSummary = GridEngine.queueSummary (
    XML.load(getClass.getResource(""))
  ).isFailure must beTrue

  def nonEmptySummary = GridEngine.queueSummary (
    XML.load(getClass.getResource("/qstat-queue-summary.xml"))
  ).get must have size 4

  def nonEmptySummaryNames = GridEngine.queueSummary (
    XML.load(getClass.getResource("/qstat-queue-summary.xml"))
  ).get map { _.name } must contain ("batch", "highmem", "mixed", "parallel").only.inOrder

  def invalidSchedule = GridEngine.runtimeSchedule (
    XML.load(getClass.getResource(""))
  ).isFailure must beTrue

  def emptySchedule = GridEngine.runtimeSchedule (
    XML.load(getClass.getResource("/qstat-job-list-empty.xml"))
  ).get must have size 0

  def nonEmptySchedule = GridEngine.runtimeSchedule (
    XML.load(getClass.getResource("/qstat-resource-job-list.xml"))
  ).get must have size 1

}
