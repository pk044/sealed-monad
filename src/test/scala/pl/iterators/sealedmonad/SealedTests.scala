package pl.iterators.sealedmonad

import cats.{Eq, Monad}
import org.scalacheck.Prop._
import org.scalacheck.{Arbitrary, Cogen}
import org.typelevel.discipline.Laws
import pl.iterators.sealedmonad.laws.SealedLaws

import scala.language.higherKinds

trait SealedTests[F[_]] extends Laws with SealedTestInstances {
  implicit def M: Monad[F]
  def laws: SealedLaws[F]

  import SealedTestInstances.ADT
  import cats.kernel.laws.discipline._

  def tests[A, B](implicit
      ArbA: Arbitrary[A],
      ArbB: Arbitrary[B],
      ArbADT: Arbitrary[ADT],
      ArbFA: Arbitrary[F[A]],
      ArbFB: Arbitrary[F[B]],
      ArbFOpt: Arbitrary[F[Option[A]]],
      ArbFAB: Arbitrary[F[Either[A, B]]],
      ArbFADT: Arbitrary[F[ADT]],
      CoA: Cogen[A],
      CoADT: Cogen[ADT],
      EqFADT: Eq[F[ADT]],
      EqFInt: Eq[F[Int]]
  ) =
    new SimpleRuleSet(
      name = "combinators",
      "value map"                                    -> forAll(laws.valueMapReduction[A, B] _),
      "result map short-circuits"                    -> forAll(laws.resultMapElimination[A, ADT] _),
      "result flatMap short-circuits"                -> forAll(laws.resultFlatMapElimination[A, ADT] _),
      "semiflatMap consistent with flatMap"          -> forAll(laws.valueSemiflatMapReduction[A, B] _),
      "result semiflatMap short-circuits"            -> forAll(laws.resultSemiflatMapElimination[A, ADT] _),
      "complete consistent with result + flatMap"    -> forAll(laws.valueCompleteIdentity[A, ADT] _),
      "completeWith consistent with complete + unit" -> forAll(laws.completeWithCoherence[A, ADT] _),
      "result complete short-circuits"               -> forAll(laws.resultCompleteElimination[A, ADT] _),
      "rethrow right does not change value"          -> forAll(laws.rethrowRightIdentity[A, ADT] _),
      "rethrow left consistent with complete"        -> forAll(laws.rethrowLeftIdentity[ADT] _),
      "attempt right consistent with map"            -> forAll(laws.attemptRightIdentity[A, B, ADT] _),
      "attempt left consistent with complete"        -> forAll(laws.attemptLeftIdentity[A, ADT] _),
      "attemptF consistent with attempt + unit"      -> forAll(laws.attemptFCoherence[A, B, ADT] _),
      "ensure true identity"                         -> forAll(laws.ensureTrueIdentity[A, ADT] _),
      "ensure false consistent with complete"        -> forAll(laws.ensureFalseIdentity[A, ADT] _),
      "ensure consistent with ensureNot"             -> forAll(laws.ensureCoherence[A, ADT] _),
      "ensure consistent with rethrow"               -> forAll(laws.ensureRethrowCoherence[A, ADT] _),
      "either identity"                              -> forAll(laws.eitherIdentity[A, ADT] _),
      "foldM consistent with flatMap"                -> forAll(laws.foldMCoherentWithFlatMap[A, ADT] _),
      "inspect does not change instance"             -> forAll(laws.inspectElimination[A, B, ADT] _),
      "valueOr"                                      -> forAll(laws.valueOrIdentity[A, ADT] _),
      "handleError"                                  -> forAll(laws.handleErrorIdentity[A, B, ADT] _),
      "semiflatMap stack-safety"                     -> lzy(laws.semiflatMapStackSafety)
      //"map stack-safety" -> lzy(laws.computationMapStackSafety)
    )
}

object SealedTests {

  def apply[F[_]](implicit ev: Monad[F]) =
    new SealedTests[F] {
      override implicit val M: Monad[F] = ev
      override val laws: SealedLaws[F]  = SealedLaws[F](ev)
    }

}
