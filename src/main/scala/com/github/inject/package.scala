package com.github

import cats.data.{Kleisli, OptionT}
import cats.{Applicative, Functor, Inject, Monad, ~>}
import cats.syntax.functor._
import cats.instances.option._
import monocle.{PIso, PLens}

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

  implicit def injectLensDerivation[A, B, M[_]](implicit
      Inj: Inject[A, B],
      This: PLens[M[A], M[B], A, B],
      That: PLens[M[B], M[A], B, A]
  ): Inject[M[A], M[B]] =
    new Inject[M[A], M[B]] {
      override def inj: M[A] => M[B] = This.modify(Inj.inj)

      override def prj: M[B] => Option[M[A]] = That.modifyF(Inj.prj)
    }
}
