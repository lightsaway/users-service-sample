package vidIq.reqres.domain

import vidIq.reqres.domain.types.{Email, FirstName, Id, LastName}

case class User(id: Id, email: Email, firstName: FirstName, lastName: LastName)
