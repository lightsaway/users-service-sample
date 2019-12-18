package vidIq.reqres.services

import io.circe.Decoder
import org.http4s.Uri
import io.circe.generic.semiauto.deriveDecoder

case class ReqResConfig(uri: Uri)

object ReqResConfig {
  import org.http4s.circe.decodeUri
  implicit val decoder: Decoder[ReqResConfig] = deriveDecoder
}
