package com.github.yarhrn.contextee

import cats.Applicative
import cats.data.ReaderT

import scala.language.higherKinds

trait Contextee[F[_], C] {
  def context: F[C]
}

object Contextee {

  implicit def contexteeForCatsReaderT[F[_] : Applicative, C] = new Contextee[Lambda[A => ReaderT[F, C, A]], C] {
    override def context: ReaderT[F, C, C] = ReaderT {
      ctx => Applicative[F].pure(ctx)
    }
  }

  def apply[F[_], C](implicit contextee: Contextee[F, C]): Contextee[F, C] = contextee

}