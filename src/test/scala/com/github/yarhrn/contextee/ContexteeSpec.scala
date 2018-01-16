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
      implicit val context: ContextHolder = ContextHolder("asd")
      val contextee: Contextee[Program, ContextHolder] = implicitly[Contextee[Program, ContextHolder]]

      val program = contextee.context.map(_.key * 2)

      program.foldMap(new ContextFreeInterpreter[Id]) should be("asdasd")

    }
  }

  case class ContextHolder(key: String)

  def program[F[_] : Monad : Contextee[?[_], ContextHolder]]: F[String] = {
    trait KStore[FG[_]] {
      def get(key: String): FG[String]
    }

    type Context[FG[_]] = Contextee[FG, ContextHolder]

    import cats.implicits._
    def program[FG[_] : Monad : KStore : Context]: FG[String] = for {
      ctx <- Contextee[FG, ContextHolder].context
      value <- implicitly[KStore[FG]].get(ctx.key)
    } yield value

    implicit def kstore[FG[_] : Monad] = new KStore[FG] {
      override def get(key: String): FG[String] = Monad[FG].pure(key + key)
    }

    program[F]
  }
}
