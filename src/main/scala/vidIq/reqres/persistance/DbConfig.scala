package vidIq.reqres.persistance
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

case class DbConfig(url: String, username: String, password: String, poolSize: Int)
object DbConfig {
  implicit val decoder: Decoder[DbConfig] = deriveDecoder
}
