package vidIq.reqres.routing

import cats.effect.Sync
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import org.http4s.EntityDecoder
import org.http4s.circe.jsonOf
import vidIq.reqres.domain.types.{Email, Id}

case class UserCreateRequest(id: Id, email: Email)

object UserCreateRequest {
  implicit def entityDecoder[F[_]: Sync]: EntityDecoder[F, UserCreateRequest] =
    jsonOf
  implicit val decoder: Decoder[UserCreateRequest] = deriveDecoder
}
