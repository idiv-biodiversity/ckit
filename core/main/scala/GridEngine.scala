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

import sys.process._
import util.Try
import xml._

object GridEngine extends GridEngine

trait GridEngine {

  object state {
    lazy val deleted   = State("deleted", Deleted)
    lazy val error     = State("error", Error)
    lazy val pending   = State("pending", Pending)
    lazy val running   = State("running", Running)
    lazy val suspended = State("suspended", Suspended)
    lazy val unknown   = State("unknown", Unknown)
  }

  def jobDetail(id: Int): Try[JobDetail] = jobDetail(xmlJobInfo(id))

  def jobList: Try[Seq[Job]] = jobList(Nil)

  def jobList(users: Seq[String], resources: Boolean = false): Try[Seq[Job]] =
    jobList(xmlJobList(users, resources))

  def runtimeSchedule: Try[RuntimeSchedule] =
    runtimeSchedule(xmlNodeJobList, xmlJobList(Nil, true), xmlReservations)

  def queueSummary: Try[Seq[QueueSummary]] = queueSummary(xmlQueueSummary)

  def nodeInfo(node: String): Try[NodeInfo] = for {
    rs <- runtimeSchedule

    slots = rs.cluster.nodes collectFirst {
      case ComputeNode(name, slots) if name == node =>
        slots
    } getOrElse 0

    jobs = rs.jobs collect {
      case ScheduleTask(nodes, id, _, start, runtime) if nodes contains node =>
        val slots = nodes(node)

        val binding = for {
          job <- Try(xmlJobInfo(id)).toOption.toList
          uaname <- job \\ "UA_name"
          text = uaname.text
          bind = Binding.SimpleCoreBinding.fromQstatXMLText(text)
        } yield bind

        val b = if (binding.nonEmpty && binding.head.isDefined) binding.head.toRight(slots) else Left(slots)

        NodeInfo.Job(id, b, start, runtime)
    }
  } yield NodeInfo(node, slots, jobs.toList)

  def exechosts: Try[IndexedSeq[String]] =
    Try("qconf -sel".!!).map(_.split("\n").toIndexedSeq)

  // -----------------------------------------------------------------------------------------------
  // testing interface
  // -----------------------------------------------------------------------------------------------

  private[ckit] lazy val DeletedRE = "d.*".r
  private[ckit] lazy val ErrorRE = "E.*".r
  private[ckit] lazy val PendingRE = "[^E]*qw".r
  private[ckit] lazy val RunningRE = "[^dE]*[rt]".r
  private[ckit] lazy val SuspendedRE = "[^dE]*[sST]".r

  private[ckit] def jobList(xml: ⇒ Elem): Try[Seq[Job]] = Try {
    xml \\ "job_list" map job
  }

  private[ckit] def jobDetail(xml: ⇒ Elem): Try[JobDetail] = Try {
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

  private[ckit] def queueSummary(xml: ⇒ Elem): Try[Seq[QueueSummary]] = Try {
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

  private[ckit] def runtimeSchedule(qhost: ⇒ Elem, qstat: ⇒ Elem, qrstat: ⇒ Elem): Try[RuntimeSchedule] = Try {
    val qhostxml: Elem = qhost

    val jobs = jobList(qstat).get.map(j ⇒ j.id → j).toMap

    val running = for {
      host     ← qhostxml \ "host"
      hostname = (host \ "@name").text if hostname != "global"
      qhostjob ← host \ "job"
      id       = (qhostjob \ "@name").text.toInt
      qstatjob ← jobs.get(id)
      jobname  = qstatjob.name
      start    = qstatjob.start
      runtime  ← qstatjob.requests.get("h_rt") flatMap { value ⇒
        Try(value.toLong * 1000).toOption
      }
    } yield ScheduleTask(Map(hostname → 1), id, jobname, start, runtime)

    val reserved = reservations(qrstat)

    RuntimeSchedule(cluster(qhostxml), running, reserved)
  }

  private[ckit] def cluster(qhost: Elem): Cluster = {
    val nodes = for {
      host  ← qhost \\ "host"
      name  = (host \ "@name").text if name != "global"
      slots = (host \ "hostvalue").collectFirst {
        case xml if (xml \ "@name").text == "num_proc" ⇒ xml.text.toInt
      }.get
    } yield ComputeNode(name, slots)

    Cluster(nodes.toSet)
  }

  private[ckit] def reservations(qrstat: Elem): Seq[ScheduleTask] = for {
    ar ← qrstat \\ "ar_summary"
    id = (ar \ "id").text.toInt
  } yield reservation(xmlReservation(id))

  private[ckit] def reservation(qrstat: Elem): ScheduleTask = {
    val xml = qrstat \ "ar_summary"

    ScheduleTask (
      nodes   = (xml \ "granted_slots_list" \ "granted_slots").map( xml ⇒
        (xml \ "@queue_instance").text.split("@")(1) → (xml \ "@slots").text.toInt
      ).toMap,
      id      = (xml \ "id").text.toInt,
      name    = (xml \ "name").text,
      start   = (xml \ "start_time").text,
      runtime = (xml \ "duration").text.split(":").reverse.toSeq.zipWithIndex.map {
        case(t,i) ⇒ (math.pow(60,i) * t.toInt).round * 1000
      }.sum
    )
  }

  // -----------------------------------------------------------------------------------------------
  // private members
  // -----------------------------------------------------------------------------------------------

  private[GridEngine] val QueueInstance = """(.+)@(.+)""".r

  private[GridEngine] def state(s: String): State = s match {
    case DeletedRE()   ⇒ state.deleted
    case ErrorRE()     ⇒ state.error
    case PendingRE()   ⇒ state.pending
    case RunningRE()   ⇒ state.running
    case SuspendedRE() ⇒ state.suspended
    case _             ⇒ state.unknown
  }

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
      state    = state((xml \ "state").text),
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

  private[GridEngine] def xmlJobList(users: Seq[String], resources: Boolean): Elem = {
    val us = if (users.isEmpty) "*" else users.mkString(",")
    val command = "qstat -xml" + { if (resources) " -r" else " " } + { " -u " + us }
    XML.loadString(command.!!)
  }

  private[GridEngine] def xmlNodeJobList: Elem = XML.loadString("qhost -xml -j".!!)

  private[GridEngine] def xmlQueueSummary: Elem = XML.loadString("qstat -xml -g c".!!)

  private[GridEngine] def xmlReservations: Elem = XML.loadString("qrstat -xml -u *".!!)

  private[ckit] def xmlReservation(id: Int): Elem = XML.loadString(s"qrstat -xml -ar $id".!!)

}
