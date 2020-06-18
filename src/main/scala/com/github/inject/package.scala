package com.github

import cats.data.{Kleisli, OptionT}
import cats.{Applicative, Functor, Inject, Monad, ~>}
import cats.syntax.functor._

package object inject {

  implicit class RichOptionT(t: OptionT.type) {
    def fromOptionK[F[_]: Applicative]: Option ~> OptionT[F, *] =
      new ~>[Option, OptionT[F, *]] {
        override def apply[A](fa: Option[A]): OptionT[F, A] =
          t.fromOption[F](fa)
      }
  }

  implicit class InjectableFunctor[F[_], A](fa: F[A]) {
    def inj[S](implicit F: Functor[F], inj: Inject[A, S]): F[S] = {
      fa.map(inj.inj)
    }
  }

  implicit class ProjectableKleisli[F[_], A, B](fa: Kleisli[F, A, B]) {
    def prj[S](implicit
        F: Monad[F],
        inj: Inject[A, S]
    ): Kleisli[OptionT[F, *], S, B] = {

      val toA = Kleisli(inj.prj).mapK(OptionT.fromOptionK[F])
      val toB = fa.mapK(OptionT.liftK)

      toA.andThen(toB)

    }
  }

}
