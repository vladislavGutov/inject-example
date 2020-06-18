package com.github

import cats.{Functor, Inject}

import cats.syntax.functor._

package object inject {

  implicit class InjectableFunctor[F[_], A](fa: F[A]) {
    def inj[S](implicit F: Functor[F], inj: Inject[A, S]): F[S] = {
      fa.map(inj.inj)
    }
  }

}
