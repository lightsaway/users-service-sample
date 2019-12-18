package vidIq.reqres.domain

import vidIq.reqres.domain.types.{Email, Id}

sealed abstract class ApplicationError(val msg: String) extends RuntimeException

case class NoExternalUserError(id: Id)
    extends ApplicationError(s"No user found in subsystem with external id  ${id}")
case class UserNotFoundError(email: Email)
    extends ApplicationError(s"No user found with email ${email.value}")
case class UserAlreadyRegistered(email: Email)
    extends ApplicationError(s"User already exists with email ${email.value}")
case class DatabaseError(message: String) extends ApplicationError(message)
case class ExternalSystemError(id: String)
    extends ApplicationError(s"There is malfunction in external system ${id}")
