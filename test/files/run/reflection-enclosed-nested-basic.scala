import scala.reflect.runtime.universe._
import scala.reflect.runtime.{currentMirror => cm}
import scala.reflect.{classTag, ClassTag}

object B {
  class B1 { override def toString = "B1"; def foo = 1 }
  private class B2 { override def toString = "B2"; def foo = 2 }
  object B3 { override def toString = "B3"; def foo = 3 }
  private object B4 { override def toString = "B4"; def foo = 4 }
  object B5 extends B1 { override def toString = "B5"; override def foo = 5 }
  private object B6 extends B2 { override def toString = "B6"; override def foo = 6 }
}

object Test extends App {
  val b = cm.moduleSymbol(classTag[B.type].runtimeClass)
  println(b)
  println(b.typeSignature.declarations.toList)

  def testMethodInvocation(instance: Any) = {
    val instanceMirror = cm.reflect(instance)
    val method = instanceMirror.symbol.typeSignature.declaration(TermName("foo")).asMethod
    val methodMirror = instanceMirror.reflectMethod(method)
    println(methodMirror())
  }

  def testNestedClass(name: String) = {
    val sym = b.typeSignature.declaration(TypeName(name)).asClass
    println(sym)
    val ctor = sym.typeSignature.declaration(nme.CONSTRUCTOR).asMethod
    val ctorMirror = cm.reflectClass(sym).reflectConstructor(ctor)
    val instance = ctorMirror()
    println(instance)
    testMethodInvocation(instance)
  }

  testNestedClass("B1")
  testNestedClass("B2")

  def testNestedModule(name: String) = {
    val sym = b.typeSignature.declaration(TermName(name)).asModule
    println(sym)
    val moduleMirror = cm.reflectModule(sym)
    val instance = moduleMirror.instance
    println(instance)
    testMethodInvocation(instance)
  }

  testNestedModule("B3")
  testNestedModule("B4")
  testNestedModule("B5")
  testNestedModule("B6")
}
