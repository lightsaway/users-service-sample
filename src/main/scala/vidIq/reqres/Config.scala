package vidIq.reqres

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import vidIq.reqres.persistance.DbConfig
import vidIq.reqres.services.ReqResConfig

case class Config(integration: ReqResConfig, dbConfig: DbConfig)

object Config {
  implicit val decoder: Decoder[Config] = deriveDecoder
}
