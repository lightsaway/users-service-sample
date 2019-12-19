package vidIq.reqres.domain

import cats.Functor
import cats.data.EitherT

trait EffectSyntax[F[_], E <: Throwable] {

  implicit class EffectImprovements[A](fe: F[Either[E, A]]) {
    def toEitherT: EitherT[F, E, A] = EitherT(fe)

  }

  implicit class EffectErrorImprovements[A, B <: Throwable](fe: F[Either[B, A]]) {
    def leftMap(f: B => E)(implicit F: Functor[F]): F[Either[E, A]] =
      Functor[F].map(fe)(e => e.left.map(f))
  }

}
