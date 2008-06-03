package org.jetbrains.plugins.scala.lang.psi.impl.toplevel.typedef

abstract class MixinNodes {
  type T

  trait Node {
    val info : T
  }
  
  case class AbstractMember(val info : T,
                            var overrides : List[AbstractMember]) extends Node

  case class ConcreteMember(val info : T,
                            var overrides : ConcreteMember,
                            var implements : List[AbstractMember],
                            var hides : List[ConcreteMember]) extends Node
}

import org.jetbrains.plugins.scala.lang.psi.types.Signature
object MethodNodes extends MixinNodes {
  type T = Signature
}

import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScReferencePattern
object FieldNodes extends MixinNodes {
  type T = ScReferencePattern
}

import org.jetbrains.plugins.scala.lang.psi.api.statements.ScTypeAlias
object TypeAliasNodes extends MixinNodes {
  type T = ScTypeAlias
}