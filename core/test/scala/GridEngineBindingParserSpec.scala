package ckit

import org.specs2._

class GridEngineBindingParserSpec extends Specification with Binding { def is = s2"""

  Grid Engine Binding Parser Specification

  Simple Core Binding
    SCCCCCCSCCCccc                                                              $scb1
                                                                                                 """
  // -----------------------------------------------------------------------------------------------
  // tests
  // -----------------------------------------------------------------------------------------------

  def scb1 = SimpleCoreBinding("SCCCCCCSCCCccc") === SimpleCoreBinding(slots = 12, cores = List(10,11,12))

}
