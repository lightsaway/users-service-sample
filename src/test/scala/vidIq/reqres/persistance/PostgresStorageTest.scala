package vidIq.reqres.persistance

import org.scalatest.{EitherValues, FunSuite, Matchers}
import vidIq.reqres.domain.{User, UserAlreadyRegistered, UserNotFoundError}
import cats.implicits._
import vidIq.reqres.DataGen
import vidIq.reqres.DataGen.randomUser

class PostgresStorageTest extends FunSuite with PostgresBaseTest with Matchers with EitherValues {

  test("save user") {
    val u   = randomUser
    val res = tx.use(p => p.save(u).value).unsafeRunSync()
    res.right.value shouldBe ((): Unit)
  }

  test("save existing user") {
    val u = randomUser

    tx.use(p => (p.save(u) *> p.save(u)).value)
      .unsafeRunSync()
      .left
      .value shouldBe a[UserAlreadyRegistered]

  }

  test("delete not existing user") {
    val u = DataGen.randomUser
    tx.use(_.delete(u.email).value).unsafeRunSync().left.value shouldBe a[UserNotFoundError]
  }

  test("delete created user") {
    val u   = DataGen.randomUser
    val res = tx.use(p => (p.save(u) *> p.delete(u.email)).value).unsafeRunSync()
    res.right.value shouldBe ((): Unit)
  }

  test("get with unknown email") {
    val u = DataGen.randomUser
    tx.use(_.get(u.email).value).unsafeRunSync().left.value shouldBe a[UserNotFoundError]
  }

  test("get known user") {
    val u   = randomUser
    val res = tx.use(p => (p.save(u) *> p.get(u.email)).value).unsafeRunSync()
    res.right.value shouldBe u
  }

}
