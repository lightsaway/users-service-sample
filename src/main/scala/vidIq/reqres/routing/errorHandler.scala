package vidIq.reqres.routing

import org.http4s.Response

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(error: E): F[Response[F]]
}
