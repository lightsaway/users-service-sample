package vidIq.reqres.domain

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveUnwrappedCodec

object types {
  case class Id(value: Long)          extends AnyVal
  case class Email(value: String)     extends AnyVal
  case class FirstName(value: String) extends AnyVal
  case class LastName(value: String)  extends AnyVal

  implicit val idCodec: Codec[Id]             = deriveUnwrappedCodec
  implicit val emailCodec: Codec[Email]       = deriveUnwrappedCodec
  implicit val fNameCodec: Codec[FirstName]   = deriveUnwrappedCodec
  implicit val lastNameCodec: Codec[LastName] = deriveUnwrappedCodec
}
