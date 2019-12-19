package vidIq.reqres.routing

import cats.MonadError
import cats.effect.Sync
import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import org.http4s.circe._
import io.scalaland.chimney.dsl._
import vidIq.reqres.domain.{
  ApplicationError,
  DatabaseError,
  ExternalSystemError,
  InvalidRequestError,
  NoExternalUserError,
  UserAlreadyRegistered,
  UserNotFoundError
}
import vidIq.reqres.domain.types.Email
import vidIq.reqres.services.{UserCreateParams, UserService}
import io.circe.syntax._

object EmailVar {
  def unapply(str: String): Option[Email] = Some(Email(str))
}

class Routes[F[_]: Sync] extends HttpErrorHandler[F, ApplicationError] with Http4sDsl[F] {
  override def handle(error: ApplicationError): F[Response[F]] = error match {
    case e: UserNotFoundError     => NotFound(Error(e.msg).asJson)
    case e: NoExternalUserError   => NotFound(Error(e.msg).asJson)
    case e: UserAlreadyRegistered => Conflict(Error(e.msg).asJson)
    case _: DatabaseError         => InternalServerError(Error("Something went wrong").asJson)
    case e: InvalidRequestError   => BadRequest(Error(e.msg).asJson)
    case _: ExternalSystemError =>
      InternalServerError(Error("Having problems with integrated services").asJson)
  }

  private def toResponse(e: Either[ApplicationError, F[Response[F]]]): F[Response[F]] = e match {
    case Left(e)  => handle(e)
    case Right(r) => r
  }

  def userRoutes(
      S: UserService[F]
  ): HttpRoutes[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "users" / EmailVar(userId) =>
        (for {
          user <- S.get(userId)
          resp = user.into[UserResponse].transform
        } yield Ok(resp)).value
          .flatMap(toResponse)
      case req @ POST -> Root / "users" => {
        (for {
          user <- req
            .attemptAs[UserCreateRequest]
            .leftMap(e => InvalidRequestError("Invalid input"))
          params = UserCreateParams(user.id, user.email)
          created <- S.create(params)
          body = created.into[UserResponse].transform
        } yield Ok(body)).value
          .flatMap(toResponse)
      }
      case DELETE -> Root / "users" / EmailVar(userId) =>
        S.delete(userId).as(NoContent()).value.flatMap(toResponse)
    }
  }
}
