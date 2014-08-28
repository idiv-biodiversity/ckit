package ckit

import collection.immutable.Seq

object Binding extends Binding

trait Binding {

  case class SimpleCoreBinding(slots: Int, cores: Seq[Int])

  object SimpleCoreBinding {
    def apply(ge: String): SimpleCoreBinding = {
      val justCores = ge.filter(_.toString.toLowerCase == "c")
      val coreIndeces = justCores.zipWithIndex.filter(_._1 == 'c').map(_._2 + 1)
      apply(slots = justCores.size, cores = coreIndeces)
    }

    def fromQstatXMLText(ge: String): Option[SimpleCoreBinding] = {
      val x = ge.replace("binding_inuse=","")
      Option(apply(ge))
    }
  }

}
