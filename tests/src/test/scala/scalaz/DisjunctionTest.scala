package scalaz

import std.AllInstances._
import scalaz.scalacheck.ScalazProperties._
import scalaz.scalacheck.ScalazArbitrary._
import org.scalacheck.Prop.forAll

object DisjunctionTest extends SpecLite {

  checkAll(order.laws[Int \/ Int])
  checkAll(monoid.laws[Int \/ Int])
  checkAll(monad.laws[Int \/ ?])
  checkAll(plus.laws[Int \/ ?])
  checkAll(traverse.laws[Int \/ ?])
  checkAll(bitraverse.laws[\/])
  checkAll(associative.laws[\/])
  checkAll(monadError.laws[\/, Int])

  "fromTryCatchThrowable" in {
    class Foo extends Throwable
    final class Bar extends Foo
    val foo = new Foo
    val bar = new Bar

    implicit val equalFoo = Equal.equalA[Foo]
    implicit val showFoo = Show.showA[Foo]
    implicit val equalBar = Equal.equalA[Bar]
    implicit val showBar = Show.showA[Bar]

    \/.fromTryCatchThrowable[Int, Foo](1) must_=== \/.right(1)
    \/.fromTryCatchThrowable[Int, Foo](throw foo) must_=== \/.left(foo)
    \/.fromTryCatchThrowable[Int, Foo](throw bar) must_=== \/.left(bar)
    \/.fromTryCatchThrowable[Int, Bar](throw foo).mustThrowA[Foo]
  }

  "recover" in {
    sealed trait Foo
    case object Bar extends Foo
    case object Baz extends Foo

    implicit val equalFoo = Equal.equalA[Foo]
    implicit val showFoo = Show.showA[Foo]

    -\/[Foo](Bar).recover({ case Bar => 1 }) must_=== \/-(1)
    -\/[Foo](Bar).recover({ case Baz => 1 }) must_=== -\/(Bar)
    \/.right[Foo, Int](1).recover({ case Bar => 4 }) must_=== \/-(1)
  }

  "recoverWith" in {
    sealed trait Foo
    case object Bar extends Foo
    case object Baz extends Foo

    implicit val equalFoo = Equal.equalA[Foo]
    implicit val showFoo = Show.showA[Foo]

    val barToBaz: PartialFunction[Foo, \/[Foo, Int]] = {
      case Bar => -\/(Baz)
    }

    val bazToInt: PartialFunction[Foo, \/[Foo, Int]] = {
      case Baz => \/-(1)
    }

    -\/[Foo](Bar).recoverWith(barToBaz) must_=== -\/(Baz)
    -\/[Foo](Bar).recoverWith(bazToInt) must_=== -\/(Bar)
    \/.right[Foo, Int](1).recoverWith(barToBaz) must_=== \/-(1)
  }
}
