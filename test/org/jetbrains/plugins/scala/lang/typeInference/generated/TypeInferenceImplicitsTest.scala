package org.jetbrains.plugins.scala.lang.typeInference
package generated

class TypeInferenceImplicitsTest extends TypeInferenceTestBase {
  //This class was generated by build script, please don't change this
  override def folderPath: String = super.folderPath + "implicits/"

  def testAmbigousConversion() {doTest()}

  def testArrayCreation() {doTest()}

  def testImplicitCall() {doTest()}

  def testImplicitCallScl1024() {doTest()}

  def testImplicitClass1() {doTest()}

  def testImplicitConversionReturnTypeBasedOnParameterType() {doTest()}

  def testImplicitParamClause() {doTest()}

  def testImplicitParameterOnlyFirstStep() {doTest()}

  def testSCL1580() {doTest()}

  def testSpecialConversion() {doTest()}

  def testSCL7475() {doTest()}

  def testSCL9877() {doTest()}

  def testSCL5854(): Unit = doTest(
    """
      |object SCL5854 {
      |
      |  case class MayErr[+E, +A](e: Either[E, A])
      |
      |  object MayErr {
      |    import scala.language.implicitConversions
      |    implicit def eitherToError[E, EE >: E, A, AA >: A](e: Either[E, A]): MayErr[EE, AA] = MayErr[E, A](e)
      |  }
      |
      |  abstract class SQLError
      |
      |  import scala.collection.JavaConverters._
      |  def convert = {
      |    val m = new java.util.HashMap[String, String]
      |    m.asScala.toMap
      |  }
      |
      |  /*start*/MayErr.eitherToError(Right(convert))/*end*/: MayErr[SQLError, Map[String, String]]
      |}
      |
      |//SCL5854.MayErr[SCL5854.SQLError, Map[String, String]]
    """.stripMargin
  )

  def testSCL9925(): Unit = {
    doTest(
      """
        |object SCL9925 {
        |
        |  abstract class Parser[+T] {
        |    def |[U >: T](x: => Parser[U]): Parser[U] = ???
        |  }
        |
        |  abstract class PerfectParser[+T] extends Parser[T]
        |
        |  implicit def parser2packrat[T](p: => Parser[T]): PerfectParser[T] = ???
        |
        |  def foo: PerfectParser[String] = ???
        |
        |  def foo1: PerfectParser[Nothing] = ???
        |
        |  def fooo4: PerfectParser[String] = /*start*/foo | foo1 | foo1/*end*/
        |}
        |
        |//SCL9925.PerfectParser[String]
      """.stripMargin)
  }


}