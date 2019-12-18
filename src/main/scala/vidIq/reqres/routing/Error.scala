package vidIq.reqres.routing
import cats.Applicative
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class Error(message: String)
object Error {
  implicit val encoder: Encoder[Error] = deriveEncoder
  implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, Error] =
    jsonEncoderOf
}
