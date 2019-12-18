package vidIq.reqres.routing

import cats.effect.Sync
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.implicits._
import io.scalaland.chimney.dsl._
import vidIq.reqres.domain.ApplicationError
import vidIq.reqres.domain.types.Email
import vidIq.reqres.services.{UserCreateParams, UserService}

object EmailVar {
  def unapply(str: String): Option[Email] = Some(Email(str))
}

object Routes {
  def userRoutes[F[_]: Sync](
      S: UserService[F]
  )(implicit H: HttpErrorHandler[F, ApplicationError]) = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    val routes = HttpRoutes.of[F] {
      case GET -> Root / "users" / EmailVar(userId) =>
        for {
          user <- S.get(userId)
          resp = user.into[UserResponse].transform
          resp <- Ok(resp)
        } yield resp
      case req @ POST -> Root / "users" => {
        for {
          user <- req.as[UserCreateRequest]
          params = UserCreateParams(user.id, user.email)
          created <- S.create(params)
          body = created.into[UserResponse].transform
          resp <- Ok(body)
        } yield resp
      }
      case DELETE -> Root / "users" / EmailVar(userId) => {
        for {
          _    <- S.delete(userId)
          resp <- NoContent()
        } yield resp
      }
    }
    H.handle(routes)
  }
}
