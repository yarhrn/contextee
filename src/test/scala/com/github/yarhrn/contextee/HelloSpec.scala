package com.github.yarhrn.contextee

import cats.Monad
import cats.data.Reader
import com.github.yarhrn.contextee.Contextee
import org.scalatest._

class ContexteeSpec extends FlatSpec with Matchers {
  "Contextee" should "be implicitly summoned for reader" in {
    trait KStore[F[_]] {
      def get(key: String): F[String]
    }
    case class Ctx(key: String)

    import cats.implicits._
    def program[F[_] : Monad : KStore : Lambda[F[_] => Contextee[F, Ctx]]]: F[String] = for {
      ctx <- Contextee[F, Ctx].context
      value <- implicitly[KStore[F]].get(ctx.key)
    } yield value

    type MonadStack[A] = Reader[Ctx, A]

    implicit def kstore[F[_]:Monad] = new KStore[F] {
      override def get(key: String): F[String] = Monad[F].pure(key + key)
    }

    import Contextee._
    program[MonadStack].run(Ctx("jk")) should be("jkjk")
  }
}
