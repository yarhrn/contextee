package com.github.yarhrn.contextee

import cats.{Applicative, InjectK, ~>}
import cats.data.{ReaderT, StateT}
import cats.free.Free

import scala.annotation.implicitNotFound
import scala.language.higherKinds

@implicitNotFound("Cant find implicit instance for ${F} with context ${C}. For Free programs please define implicit val context:${C}")
trait Contextee[F[_], C] {
  def context: F[C]
}

object Contextee {

  implicit def contexteeForCatsReaderT[F[_] : Applicative, C] = new Contextee[Lambda[A => ReaderT[F, C, A]], C] {
    override def context: ReaderT[F, C, C] = ReaderT {
      ctx => Applicative[F].pure(ctx)
    }
  }

  implicit def contexteeForCatsStateT[F[_] : Applicative, C, A] = new Contextee[Lambda[A => StateT[F, C, A]],C] {
    override def context = StateT.get
  }

  def apply[F[_], C](implicit contextee: Contextee[F, C]): Contextee[F, C] = contextee

}

sealed trait ContexteeAlgebra[A]

case class GetContext[C](ctx: C) extends ContexteeAlgebra[C]

class ContexteeFree[F[_], C](ctx: C)(implicit I: InjectK[ContexteeAlgebra, F]) extends Contextee[Lambda[A => Free[F, A]], C] {
  def context: Free[F, C] = Free.inject[ContexteeAlgebra, F](GetContext(ctx))
}

object ContexteeFree {
  implicit def interacts[F[_], C](implicit I: InjectK[ContexteeAlgebra, F], ctx: C): ContexteeFree[F, C] = new ContexteeFree[F, C](ctx)
}

class ContextFreeInterpreter[F[_] : Applicative] extends (ContexteeAlgebra ~> F) {
  def apply[A](i: ContexteeAlgebra[A]) = i match {
    case GetContext(ctx) => Applicative[F].pure(ctx)
  }
}