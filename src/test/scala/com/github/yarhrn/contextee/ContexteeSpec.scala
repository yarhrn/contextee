package com.github.yarhrn.contextee

import cats.{Id, Monad}
import cats.data.{Reader, State, StateT}
import cats.free.Free
import org.scalatest._

import scala.language.higherKinds

class ContexteeSpec extends FreeSpec with Matchers {
  "Contextee should" - {

    "be implicitly summoned for reader" in {
      trait KStore[F[_]] {
        def get(key: String): F[String]
      }
      case class Ctx(key: String)
      type Context[F[_]] = Contextee[F, Ctx]

      import cats.implicits._
      def program[F[_] : Monad : KStore : Context]: F[String] = for {
        ctx <- Contextee[F, Ctx].context
        value <- implicitly[KStore[F]].get(ctx.key)
      } yield value

      type MonadStack[A] = Reader[Ctx, A]

      implicit def kstore[F[_] : Monad] = new KStore[F] {
        override def get(key: String): F[String] = Monad[F].pure(key + key)
      }

      import Contextee._
      program[MonadStack].run(Ctx("jk")) should be("jkjk")
    }

    "be implicitly summoned for state" in {
      trait KStore[F[_]] {
        def get(key: String): F[String]
      }
      case class Ctx(key: String)
      type Context[F[_]] = Contextee[F, Ctx]

      import cats.implicits._
      def program[F[_] : Monad : KStore : Context]: F[String] = for {
        ctx <- Contextee[F, Ctx].context
        value <- implicitly[KStore[F]].get(ctx.key)
      } yield value

      type MonadStack[A] = State[Ctx, A]

      implicit def kstore[F[_] : Monad] = new KStore[F] {
        override def get(key: String): F[String] = Monad[F].pure(key + key)
      }

      import Contextee._
      program[MonadStack].run(Ctx("jk")).value._2 should be("jkjk")
    }

    "be usable in free application" in {
      import ContexteeFree._

      case class Ctx(s: String)
      type Program[A] = Free[ContexteeAlgebra, A]
      implicit val ctx = Ctx("asd")
      val a: Contextee[Program, Ctx] = implicitly[Contextee[Program, Ctx]]

      a.context.map(_.s * 2).foldMap(new ContextFreeInterpreter[Id]) should be("asdasd")

    }
  }

}
