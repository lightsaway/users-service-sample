package vidIq.reqres.routing

import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class UserResponse(id: Long, email: String, firstName: String, lastName: String)

object UserResponse {

  implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, UserResponse] =
    jsonEncoderOf
  implicit val encoder: Encoder[UserResponse] = deriveEncoder
}
