package vidIq.reqres.services

import cats.effect.Sync
import cats.implicits._
import io.circe.{Decoder, HCursor}
import org.http4s._
import org.http4s.circe.jsonOf
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.dsl.io._
import vidIq.reqres.domain.types.{FirstName, Id, LastName}
import vidIq.reqres.domain.{ExternalSystemError, NoExternalUserError, UserNotFoundError}

case class UserData(firstName: FirstName, lastName: LastName)

object UserData {
  implicit val decodeUserData: Decoder[UserData] = (c: HCursor) => {
    val data = c.downField("data")
    for {
      fName <- data.downField("first_name").as[String]
      lName <- data.downField("last_name").as[String]
    } yield UserData(FirstName(fName), LastName(lName))
  }
  implicit def userDataEntityDecoder[F[_]: Sync]: EntityDecoder[F, UserData] =
    jsonOf
}

trait ReqResService[F[_]] {
  def fetchUserData(id: Id): F[UserData]
}

class ReqResServiceImpl[F[_]: Sync](endpoint: Uri, client: Client[F])
    extends ReqResService[F]
    with Http4sClientDsl[F] {

  override def fetchUserData(id: Id): F[UserData] = {
    client
      .expect[UserData](GET(endpoint.withPath(s"/api/users/${id.value}")))
      .adaptError {
        case _: DecodeFailure => NoExternalUserError(id)
        case _                => ExternalSystemError("ReqRes.com")
      }
  }
}
