package vidIq.reqres.persistance

import org.scalatest.{FunSuite, Matchers}
import vidIq.reqres.domain.{User, UserAlreadyRegistered, UserNotFoundError}
import cats.implicits._
import vidIq.reqres.DataGen
import vidIq.reqres.DataGen.randomUser

class PostgresStorageTest extends FunSuite with PostgresBaseTest with Matchers {

  test("save user") {
    val u   = randomUser
    val res = tx.use(p => p.save(u)).unsafeRunSync()
    res shouldBe ((): Unit)
  }

  test("save existing user") {
    val u = randomUser
    assertThrows[UserAlreadyRegistered] { tx.use(p => p.save(u) *> p.save(u)).unsafeRunSync() }

  }

  test("delete not existing user") {
    val u = DataGen.randomUser
    assertThrows[UserNotFoundError] { tx.use(_.delete(u.email)).unsafeRunSync() }
  }

  test("delete created user") {
    val u   = DataGen.randomUser
    val res = tx.use(p => p.save(u) *> p.delete(u.email)).unsafeRunSync()
    res shouldBe ((): Unit)
  }

  test("get with unknown email") {
    val u = DataGen.randomUser
    assertThrows[UserNotFoundError] { tx.use(_.get(u.email)).unsafeRunSync() }
  }

  test("get known user") {
    val u   = randomUser
    val res = tx.use(p => p.save(u) *> p.get(u.email)).unsafeRunSync()
    res shouldBe u
  }

}
