package org.jetbrains.plugins.scala
package lang
package psi
package stubs
package elements


import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.{StubElement, StubInputStream, StubOutputStream}
import com.intellij.util.io.StringRef
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.imports.ScImportSelector
import org.jetbrains.plugins.scala.lang.psi.impl.toplevel.imports.ScImportSelectorImpl
import org.jetbrains.plugins.scala.lang.psi.stubs.impl.ScImportSelectorStubImpl

/**
  * User: Alexander Podkhalyuzin
  * Date: 20.06.2009
  */
class ScImportSelectorElementType[Func <: ScImportSelector]
  extends ScStubElementType[ScImportSelectorStub, ScImportSelector]("import selector") {
  override def serialize(stub: ScImportSelectorStub, dataStream: StubOutputStream): Unit = {
    dataStream.writeName(stub.asInstanceOf[ScImportSelectorStubImpl[_ <: PsiElement]].referenceText.toString)
    dataStream.writeName(stub.importedName)
    dataStream.writeBoolean(stub.isAliasedImport)
  }

  override def deserialize(dataStream: StubInputStream, parentStub: StubElement[_ <: PsiElement]): ScImportSelectorStub = {
    val refText = StringRef.toString(dataStream.readName)
    val importedName = StringRef.toString(dataStream.readName)
    val aliasImport = dataStream.readBoolean()
    new ScImportSelectorStubImpl(parentStub, this, refText, importedName, aliasImport)
  }

  override def createStub(psi: ScImportSelector, parentStub: StubElement[_ <: PsiElement]): ScImportSelectorStub =
    new ScImportSelectorStubImpl(parentStub, this,
      psi.reference.getText, psi.importedName, psi.isAliasedImport)

  override def createElement(node: ASTNode): ScImportSelector = new ScImportSelectorImpl(node)

  override def createPsi(stub: ScImportSelectorStub): ScImportSelector = new ScImportSelectorImpl(stub)
}