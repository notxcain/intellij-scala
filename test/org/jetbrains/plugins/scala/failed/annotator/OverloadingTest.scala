package org.jetbrains.plugins.scala.failed.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.psi.{PsiErrorElement, PsiReference}
import org.jetbrains.plugins.scala.annotator.AnnotatorHolderMock
import org.jetbrains.plugins.scala.annotator.quickfix.ReportHighlightingErrorQuickFix
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.jetbrains.plugins.scala.lang.psi.api.base.types.{ScTypeElement, ScTypeElementExt}
import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScBlockExpr, ScExpression}
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScPatternDefinition
import org.jetbrains.plugins.scala.lang.psi.types.api.ScTypePresentation
import org.jetbrains.plugins.scala.lang.psi.types.ScTypeExt
import org.jetbrains.plugins.scala.{PerfCycleTests, ScalaBundle}
import org.junit.Assert._
import org.junit.experimental.categories.Category

/**
  * User: Dmitry.Naydanov
  * Date: 23.03.16.
  * 
  *  
  */
@Category(Array(classOf[PerfCycleTests]))
class OverloadingTest extends ScalaLightCodeInsightFixtureTestAdapter {
  //TODO this class contains a fair amount of a copy-paste code, however refactoring isn't practical here as the class is to be removed soon 
  import org.jetbrains.plugins.scala.extensions._

  protected def collectMessages(fileText: String) = {
    myFixture.configureByText("dummy.scala", fileText)
    val file = myFixture.getFile
    val mock = new AnnotatorHolderMock(file)

    assertEquals(Nil, file.depthFirst.filterByType(classOf[PsiErrorElement]).map(_.getText).toList)

    assertEquals(Nil, file.depthFirst.filterByType(classOf[PsiReference])
      .filter(_.resolve == null).map(_.getElement.getText).toList)

    file.depthFirst.foreach {
      case it: ScPatternDefinition => annotate(it, mock, typeAware = true)
      case _ => 
    }
    
    mock.annotations
  }

  protected def annotate(element: ScPatternDefinition, holder: AnnotationHolder, typeAware: Boolean): Unit = {
    for (expr <- element.expr; element <- element.children.findByType(classOf[ScTypeElement]))
      checkConformance(expr, element, holder)
  }

  private def checkConformance(expression: ScExpression, typeElement: ScTypeElement, holder: AnnotationHolder) {
    expression.getTypeAfterImplicitConversion().tr.foreach {actual =>
      val expected = typeElement.calcType
      if (!actual.conforms(expected)(typeElement.typeSystem)) {
        val expr = expression match {
          case b: ScBlockExpr => b.getRBrace.map(_.getPsi).getOrElse(b)
          case _ => expression
        }
        val (actualText, expText) = ScTypePresentation.different(actual, expected)
        val annotation = holder.createErrorAnnotation(expr,
          ScalaBundle.message("type.mismatch.found.required", actualText, expText))
        annotation.registerFix(ReportHighlightingErrorQuickFix)
      }
    }
  }

  def testSCL9908(): Unit = assert(
    collectMessages(
      """
        |class Test { 
        |  def foo(s: String, args: Any*) = println("foo(s, args)") 
        |  def foo(x: Any) = println("foo(x)") 
        | 
        |  def func(args: Array[String]) = { 
        |    foo("Hello") // red code; 'foo(s, args)' with scalac 
        |  } 
        |}
      """.stripMargin).isEmpty
  )

  def testSCL7442(): Unit = assert(
    collectMessages(
      """
        |class Test { 
        |  def set(value: Any) : Unit = {
        |    val (a, b, c, d) = value.asInstanceOf[(Int, Int, Int, Int)]
        |    set(a, b, c, d)
        |  }
        |  def set(aValue: Int, bValue: Int, cValue: Int, dValue: Int) = {
        |    //...
        |  }
        |  (set _).tupled((1, 2, 3, 4))
        |}
      """.stripMargin).isEmpty
  )

  def testSCL10158(): Unit = assert(
    collectMessages(
      """
        |class Test { 
        |  val lock = new AnyRef
        |  class Test {
        |    def run: Unit = this.synchronized(println("sync"))
        |    def synchronized[T](exec: => T): Unit = lock.synchronized(exec)
        |  }
        |}
      """.stripMargin).isEmpty
  )

  def testSCL10183(): Unit = assert(
    collectMessages(
      """
        |class MyClass {
        |  def foo[T](): T = ???
        |
        |  val value = foo[MyTrait]
        |  value.get _
        |}
        |
        |trait MyTrait {
        |  def get() = ???
        |  def get[A](arg: Any => Any) = ???
        |}
      """.stripMargin).isEmpty
  )

  def testSCL10295(): Unit = assert(
    collectMessages(
      """
        |import java.lang.reflect.Field
        |import scala.collection.mutable
        |
        |class Test {
        |
        |  def instanceFieldsOf(v: AnyRef): Array[Field] = ???
        |  def instanceFieldsOf(v: AnyRef,
        |                       cache: mutable.Map[Class[_], Array[Field]],
        |                       newFieldsHandler: Field => Unit = v => ())
        |  : Array[Field] = ???
        |
        |  def valueAndInstanceFieldTuplesOf(v: AnyRef,
        |                                    cache: mutable.Map[Class[_], Array[Field]],
        |                                    newFieldsHandler: Field => Unit = v => ())
        |  : Stream[(AnyRef, Field)] = {
        |    val fields: Array[Field] = this.instanceFieldsOf(v, cache, newFieldsHandler)
        |    fields.toStream.map { f => (f.get(v), f) }
        |  }
        |}
      """.stripMargin).isEmpty
  )
}
