package com.github.yarhrn.contextee

import cats.data.{Reader, State}
import cats.free.Free
import cats.{Id, Monad}
import org.scalatest._

import scala.language.higherKinds

class ContexteeSpec extends FreeSpec with Matchers {
  "Contextee should" - {

    "be implicitly summoned for reader" in {
      type MonadStack[A] = Reader[ContextHolder, A]
      import Contextee._
      program[MonadStack].run(ContextHolder("jk")) should be("jkjk")
    }

    "be implicitly summoned for state" in {
      type MonadStack[A] = State[ContextHolder, A]
      import Contextee._
      program[MonadStack].run(ContextHolder("jk")).value._2 should be("jkjk")
    }

    "be usable in free application" in {
      import ContexteeFree._

      type Program[A] = Free[ContexteeAlgebra, A]
      implicit val context = ContextHolder("asd")
      val contextee: Contextee[Program, ContextHolder] = implicitly[Contextee[Program, ContextHolder]]

      val program = contextee.context.map(_.key * 2)

      program.foldMap(new ContextFreeInterpreter[Id]) should be("asdasd")

    }
  }

  case class ContextHolder(key: String)

  def program[F[_] : Monad : Lambda[F[_] => Contextee[F, ContextHolder]]]: F[String] = {
    trait KStore[F[_]] {
      def get(key: String): F[String]
    }

    type Context[F[_]] = Contextee[F, ContextHolder]

    import cats.implicits._
    def program[F[_] : Monad : KStore : Context]: F[String] = for {
      ctx <- Contextee[F, ContextHolder].context
      value <- implicitly[KStore[F]].get(ctx.key)
    } yield value

    implicit def kstore[F[_] : Monad] = new KStore[F] {
      override def get(key: String): F[String] = Monad[F].pure(key + key)
    }

    program[F]
  }
}
