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

import sys.process._
import util.Try
import xml._

object GridEngine extends GridEngine

trait GridEngine {

  def jobDetail(id: Int): Try[JobDetail] = jobDetail(xmlJobInfo(id))

  def jobList: Try[Seq[Job]] = jobList(xmlJobList())

  def jobList(user: Option[String] = None, resources: Boolean = false): Try[Seq[Job]] =
    jobList(xmlJobList(user, resources))

  def runtimeSchedule: Try[Seq[(String,Int,String,String,Int)]] =
    runtimeSchedule(xmlNodeJobList, xmlJobList(resources = true))

  def queueSummary: Try[Seq[QueueSummary]] = queueSummary(xmlQueueSummary)

  // -----------------------------------------------------------------------------------------------
  // testing interface
  // -----------------------------------------------------------------------------------------------

  private[daemon] def jobList(xml: ⇒ Elem): Try[Seq[Job]] = Try {
    xml \\ "job_list" map job
  }

  private[daemon] def jobDetail(xml: ⇒ Elem): Try[JobDetail] = Try {
    def requests(xml: ⇒ NodeSeq): Seq[(String,String)] = xml \ "qstat_l_requests" map { xml ⇒
      (xml \ "CE_name").text → (xml \ "CE_stringval").text
    }

    def taskUsage(xml: ⇒ NodeSeq): Seq[(String,String)] = for {
      elem  ← xml \ "scaled"
      name  = (elem \ "UA_name").text
      value = (elem \ "UA_value").text
    } yield name → value

    def tasks(xml: ⇒ NodeSeq): Seq[Task] = for {
      task  ← xml \ "ulong_sublist"
      id    = (task \ "JAT_task_number").text if id.nonEmpty
      usage = taskUsage(task \ "JAT_scaled_usage_list").toMap if usage.nonEmpty
    } yield Task(id.toInt, usage)

    def messages(xml: ⇒ NodeSeq): Seq[String] = for {
      message ← xml \\ "MES_message"
    } yield message.text

    JobDetail (
      name           = (xml \\ "JB_job_name").text,
      id             = (xml \\ "JB_job_number").text,
      owner          = (xml \\ "JB_owner").text,
      group          = (xml \\ "JB_group").text,
      project        = (xml \\ "JB_project").text,
      account        = (xml \\ "JB_account").text,
      requests       = requests(xml \\ "JB_hard_resource_list").toMap,
      tasks          = tasks(xml \\ "JB_ja_tasks"),
      messages       = messages(xml \\ "SME_message_list"),
      globalMessages = messages(xml \\ "SME_global_message_list")
    )
  }

  private[daemon] def queueSummary(xml: ⇒ Elem): Try[Seq[QueueSummary]] = Try {
    xml \ "cluster_queue_summary" map { xml ⇒
      QueueSummary (
        name                 = (xml \ "name").text,
        load                 = (xml \ "load").text.toDouble,
        used                 = (xml \ "used").text.toInt,
        reserved             = (xml \ "resv").text.toInt,
        available            = (xml \ "available").text.toInt,
        total                = (xml \ "total").text.toInt,
        temporaryUnavailable = (xml \ "temp_disabled").text.toInt,
        unavailable          = (xml \ "manual_intervention").text.toInt
      )
    }
  }

  private[daemon] def runtimeSchedule(qhost: ⇒ Elem, qstat: ⇒ Elem): Try[Seq[(String,Int,String,String,Int)]] = Try {
    val qhostxml: Elem = qhost

    val jobs = jobList(qstat) map { _.map(j ⇒ j.id → j).toMap } getOrElse Map()

    for {
      host     ← qhostxml \ "host"
      hostname = (host \ "@name").text if hostname != "global"
      qhostjob ← host \ "job"
      id       = (qhostjob \ "@name").text.toInt
      qstatjob = jobs(id)
      jobname  = qstatjob.name
      start    = qstatjob.start
      runtime  = qstatjob.requests.get("h_rt") flatMap { value ⇒
        Try(value.toInt).toOption
      }
    } yield (hostname, id, jobname, start, runtime)
  } map {
    _ collect {
      case (host, id, name, start, Some(rt)) ⇒ (host, id, name, start, rt)
    }
  }

  // -----------------------------------------------------------------------------------------------
  // private members
  // -----------------------------------------------------------------------------------------------

  private[GridEngine] val QueueInstance = """(.+)@(.+)""".r

  private[GridEngine] def job(xml: Node): Job = {
    val (q,n) = (xml \ "queue_name").text.trim match {
      case QueueInstance(q,n) ⇒ (q,n)
      case _                  ⇒ ("","")
    }

    val rs = xml \ "hard_request" map { xml ⇒
      val name  = (xml \ "@name").text
      val value = xml.text
      name → value
    }

    Job (
      id       = (xml \ "JB_job_number").text.toInt,
      priority = (xml \ "JAT_prio").text.toDouble,
      name     = (xml \ "JB_name").text,
      owner    = (xml \ "JB_owner").text,
      state    = (xml \ "state").text,
      start    = (xml \ "JAT_start_time").text,
      queue    = q,
      node     = n,
      slots    = (xml \ "slots").text.toInt,
      requests = rs.toMap
    )
  }

  // -----------------------------------------------------------------------------------------------
  // direct interface to the grid engine
  // -----------------------------------------------------------------------------------------------

  private[GridEngine] def xmlJobInfo(id: Int): Elem = XML.loadString(s"qstat -xml -j $id".!!)

  private[GridEngine] def xmlJobList(user: Option[String] = None, resources: Boolean = false): Elem = {
    val command = "qstat -xml" + { if (resources) " -r" else " " } + { " -u " + user.getOrElse("*") }
    XML.loadString(command)
  }

  private[GridEngine] def xmlNodeJobList: Elem = XML.loadString("qhost -xml -j".!!)

  private[GridEngine] def xmlQueueSummary: Elem = XML.loadString("qstat -xml -g c".!!)

}
