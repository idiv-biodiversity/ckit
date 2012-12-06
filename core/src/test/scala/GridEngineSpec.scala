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
package daemon

import language.postfixOps

import org.specs2._

class GridEngineSpec extends Specification with GridEngine { def is =

  // -----------------------------------------------------------------------------------------------
  // fragments
  // -----------------------------------------------------------------------------------------------

  "Grid Engine Specification"                                                                      ^
                                                                                                  p^
  "Job State Parsing"                                                                              ^
    "Deleted States"                                                                               ^
      """parsing state "dr""""                                        ! dparse("dr")               ^
      """parsing state "dt""""                                        ! dparse("dt")               ^
      """parsing state "dRr""""                                       ! dparse("dRr")              ^
      """parsing state "ds""""                                        ! dparse("ds")               ^
      """parsing state "dS""""                                        ! dparse("dS")               ^
      """parsing state "dT""""                                        ! dparse("dT")               ^
      """parsing state "dRs""""                                       ! dparse("dRs")              ^
      """parsing state "dRS""""                                       ! dparse("dRS")              ^
      """parsing state "dRT""""                                       ! dparse("dRT")           ^bt^
    "Error States"                                                                                 ^
      """parsing state "Eqw""""                                       ! eparse("Eqw")              ^
      """parsing state "Ehqw""""                                      ! eparse("Ehqw")             ^
      """parsing state "EhRqw""""                                     ! eparse("EhRqw")         ^bt^
    "Pending States"                                                                               ^
      """parsing state "qw""""                                        ! pparse("qw")               ^
      """parsing state "hqw""""                                       ! pparse("hqw")              ^
      """parsing state "hRqw""""                                      ! pparse("hRqw")          ^bt^
    "Running States"                                                                               ^
      """parsing state "r""""                                         ! rparse("r")                ^
      """parsing state "t""""                                         ! rparse("t")                ^
      """parsing state "Rr""""                                        ! rparse("Rr")               ^
      """parsing state "Rt""""                                        ! rparse("Rt")            ^bt^
    "Suspended States"                                                                             ^
      """parsing state "s""""                                         ! sparse("s")                ^
      """parsing state "ts""""                                        ! sparse("ts")               ^
      """parsing state "S""""                                         ! sparse("S")                ^
      """parsing state "tS""""                                        ! sparse("tS")               ^
      """parsing state "T""""                                         ! sparse("T")                ^
      """parsing state "tT""""                                        ! sparse("tT")               ^
      """parsing state "Rs""""                                        ! sparse("Rs")               ^
      """parsing state "Rts""""                                       ! sparse("Rts")              ^
      """parsing state "RS""""                                        ! sparse("RS")               ^
      """parsing state "RtS""""                                       ! sparse("RtS")              ^
      """parsing state "RT""""                                        ! sparse("RT")               ^
      """parsing state "RtT""""                                       ! sparse("RtT")           ^bt^
                                                                                                  p^
  "Job List Parsing"                                                                               ^
    "invalid target yields failure"                                   ! invalidList                ^
    "empty target yields empty collection"                            ! emptyList                  ^
    "valid input yields non empty collection"                         ! nonEmptyList               ^
                                                                                                  p^
  "Job Detail Parsing"                                                                             ^
    "invalid target yields failure"                                   ! invalidDetail              ^
    "empty target yields failure"                                     ! emptyDetail                ^
    "valid input yields success (sequential job)"                     ! seqDetail                ^t^
      "which has one task"                                            ! seqDetailTasks             ^
      "which has two requests"                                        ! seqDetailRequests       ^bt^
    "valid input yields success (parallel job)"                       ! parDetail                ^t^
      "which has one task"                                            ! parDetailTasks             ^
      "which has two requests"                                        ! parDetailRequests       ^bt^
    "valid input yields success (array job)"                          ! arrDetail                ^t^
      "which has ten tasks"                                           ! arrDetailTasks             ^
      "which has two requests"                                        ! arrDetailRequests       ^bt^
    "valid input yields success (waiting job)"                        ! waitDetail               ^t^
      "which has no task"                                             ! waitDetailTasks            ^
      "which has two requests"                                        ! waitDetailRequests         ^
      "which has non empty messages"                                  ! waitDetailMessages      ^bt^
                                                                                                  p^
  "Queue Summary Parsing"                                                                          ^
    "invalid target yields failure"                                   ! invalidSummary             ^
    "valid input yields non empty collection"                         ! nonEmptySummary          ^t^
      "contains correct names of queues"                              ! nonEmptySummaryNames    ^bt^
                                                                                                  p^
  "Runtime Schedule Parsing"                                                                       ^
    "invalid target yields failure"                                   ! invalidSchedule            ^
    "empty target yields empty collection"                            ! emptySchedule              ^
    "valid input yields non empty collection"                         ! nonEmptySchedule           ^
                                                                                                 end
  // -----------------------------------------------------------------------------------------------
  // tests
  // -----------------------------------------------------------------------------------------------

  def invalidList = list("").isFailure must beTrue
  def emptyList = list("/qstat-job-list-empty.xml").get must have size 0
  def nonEmptyList = list("/qstat-job-list.xml").get must have size 2

  def invalidDetail = detail("").isFailure must beTrue
  def emptyDetail = detail("/qstat-job-detail-empty.xml").isFailure must beTrue
  def seqDetail = detail("/qstat-job-detail-sequential.xml").isSuccess must beTrue
  def seqDetailTasks = detail("/qstat-job-detail-sequential.xml").get.tasks must have size 1
  def seqDetailRequests = detail("/qstat-job-detail-sequential.xml").get.requests must have size 2
  def parDetail = detail("/qstat-job-detail-parallel.xml").isSuccess must beTrue
  def parDetailTasks = detail("/qstat-job-detail-parallel.xml").get.tasks must have size 1
  def parDetailRequests = detail("/qstat-job-detail-parallel.xml").get.requests must have size 2
  def arrDetail = detail("/qstat-job-detail-array-job.xml").isSuccess must beTrue
  def arrDetailTasks = detail("/qstat-job-detail-array-job.xml").get.tasks must have size 10
  def arrDetailRequests = detail("/qstat-job-detail-array-job.xml").get.requests must have size 2
  def waitDetail = detail("/qstat-job-detail-waiting.xml").isSuccess must beTrue
  def waitDetailTasks = detail("/qstat-job-detail-waiting.xml").get.tasks must have size 0
  def waitDetailRequests = detail("/qstat-job-detail-waiting.xml").get.requests must have size 2
  def waitDetailMessages = detail("/qstat-job-detail-waiting.xml").get.messages must not be empty

  def invalidSummary = summary("").isFailure must beTrue
  def nonEmptySummary = summary("/qstat-queue-summary.xml").get must have size 4
  def nonEmptySummaryNames = summary("/qstat-queue-summary.xml").get map {
    _.name
  } must contain ("batch", "highmem", "mixed", "parallel").only.inOrder

  def invalidSchedule = schedule("", "").isFailure must beTrue
  def emptySchedule = schedule("/qhost-jobs-empty.xml", "/qstat-job-list-empty.xml").get must have size 0
  def nonEmptySchedule = schedule("/qhost-jobs.xml", "/qstat-resource-job-list.xml").get must have size 1

  import Job._

  def dparse(state: String) = DeletedRE.unapplySeq(state) must beSome
  def eparse(state: String) = ErrorRE.unapplySeq(state) must beSome
  def pparse(state: String) = PendingRE.unapplySeq(state) must beSome
  def rparse(state: String) = RunningRE.unapplySeq(state) must beSome
  def sparse(state: String) = SuspendedRE.unapplySeq(state) must beSome

  // -----------------------------------------------------------------------------------------------
  // util
  // -----------------------------------------------------------------------------------------------

  def XML(res: String) = scala.xml.XML.load(getClass.getResource(res))
  def list(res: String) = jobList(XML(res))
  def detail(res: String) = jobDetail(XML(res))
  def summary(res: String) = queueSummary(XML(res))
  def schedule(r1: String, r2: String) = runtimeSchedule(XML(r1), XML(r2))

}
