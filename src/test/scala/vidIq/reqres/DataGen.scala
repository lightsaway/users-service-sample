package vidIq.reqres

import vidIq.reqres.domain.User
import vidIq.reqres.domain.types.{Email, FirstName, Id, LastName}

import scala.util.Random

object DataGen {

  def staticUser: User = User(Id(1), Email("first@gmail.com"), FirstName("Man"), LastName("Hero"))

  def randomUser: User = {
    val rand = Random.alphanumeric
    User(
      Id(Random.nextLong()),
      Email(rand.take(50).mkString + "@gmail.com"),
      FirstName(rand.take(50).mkString),
      LastName(rand.take(50).mkString)
    )
  }

}
