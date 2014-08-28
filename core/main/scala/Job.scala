package ckit

object State {
  implicit val ordering: Ordering[State] = new Ordering[State] {
    override def compare(a: State, b: State): Int = {
      a.category.id.compareTo(b.category.id)
    }
  }
}

case class State(name: String, category: StateCategory)

case class Job (
    id: Int,
    priority: Double,
    name: String,
    owner: String,
    state: State,
    start: String,
    queue: String,
    node: String,
    slots: Int,
    requests: Map[String,String]
  )

case class JobList(jobs: Seq[Job])

case class JobDetail (
    name: String,
    id: String,
    owner: String,
    group: String,
    project: String,
    account: String,
    requests: Map[String,String],
    tasks: Seq[Task],
    messages: Seq[String],
    globalMessages: Seq[String]
  )

case class ComputeNode(name: String, slots: Int)

case class Cluster(nodes: Set[ComputeNode])

case class Task(id: Int, usage: Map[String,String])

case class ScheduleTask(nodes: Map[String,Int], id: Int, name: String, start: String, runtime: Long)

case class RuntimeSchedule(cluster: Cluster, jobs: Seq[ScheduleTask], reservations: Seq[ScheduleTask])

import Binding._

object NodeInfo {
  case class Job(id: Int, slots: Either[Int,SimpleCoreBinding], start: String, runtime: Long)
}

case class NodeInfo(name: String, slots: Int, jobs: List[NodeInfo.Job])

case class NodeList(nodes: IndexedSeq[String])
case class ListNodeInfo(node: List[NodeInfo])
