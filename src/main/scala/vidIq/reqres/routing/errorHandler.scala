package vidIq.reqres.routing

import cats.{ApplicativeError, MonadError}
import cats.data.{Kleisli, OptionT}
import cats.implicits._
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{HttpRoutes, Request, Response}
import vidIq.reqres.domain.{
  ApplicationError,
  DatabaseError,
  ExternalSystemError,
  NoExternalUserError,
  UserAlreadyRegistered,
  UserNotFoundError
}

trait HttpErrorHandler[F[_], E <: Throwable] {
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

class UserHttpErrorHandler[F[_]](implicit M: MonadError[F, ApplicationError])
    extends HttpErrorHandler[F, ApplicationError]
    with Http4sDsl[F] {

  private val handler: ApplicationError => F[Response[F]] = {
    case e: UserNotFoundError     => NotFound(Error(e.msg).asJson)
    case e: NoExternalUserError   => NotFound(Error(e.msg).asJson)
    case e: UserAlreadyRegistered => Conflict(Error(e.msg).asJson)
    case _: DatabaseError         => InternalServerError(Error("Something went wrong").asJson)
    case e: ExternalSystemError =>
      InternalServerError(Error("Having problems with integrated services").asJson)
  }

  override def handle(routes: HttpRoutes[F]): HttpRoutes[F] =
    RoutesHttpErrorHandler(routes)(handler)
}

object RoutesHttpErrorHandler {
  def apply[F[_], E <: Throwable](
      routes: HttpRoutes[F]
  )(handler: E => F[Response[F]])(implicit ev: ApplicativeError[F, E]): HttpRoutes[F] =
    Kleisli { req: Request[F] =>
      OptionT {
        routes.run(req).value.handleErrorWith { e =>
          handler(e).map(Option(_))
        }
      }
    }
}
