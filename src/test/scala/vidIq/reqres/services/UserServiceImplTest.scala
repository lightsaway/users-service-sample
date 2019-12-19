package vidIq.reqres.services

import cats.data.EitherT
import cats.effect.IO
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.{EitherValues, FunSuite, Matchers}
import vidIq.reqres.DataGen
import vidIq.reqres.persistance.Storage
import cats.implicits._
import vidIq.reqres.domain.{
  ApplicationError,
  ExternalSystemError,
  NoExternalUserError,
  User,
  UserAlreadyRegistered
}
import vidIq.reqres.domain.types.Id
import io.scalaland.chimney.dsl._

class UserServiceImplTest
    extends FunSuite
    with Matchers
    with IdiomaticMockito
    with ArgumentMatchersSugar
    with EitherValues {

  case class Fixture(db: Storage[IO], reqRes: ReqResService[IO], userService: UserServiceImpl[IO])

  def getFixture: Fixture = {
    val reqRes      = mock[ReqResService[IO]]
    val db          = mock[Storage[IO]]
    val userService = new UserServiceImpl[IO](db, reqRes)
    Fixture(db, reqRes, userService)
  }

  test("create user that exists in reqresp service") {
    val u       = DataGen.randomUser
    val fixture = getFixture

    fixture.db.save(any[User]) returns EitherT.fromEither[IO](().asRight[ApplicationError])
    fixture.reqRes.fetchUserData(any[Id]) returns EitherT.fromEither[IO](
      u.into[UserData].transform.asRight
    )
    fixture.userService
      .create(UserCreateParams(u.id, u.email))
      .value
      .unsafeRunSync()
      .right
      .value shouldBe u
  }

  test("create user that doesn't exists in reqresp service") {
    val u       = DataGen.randomUser
    val fixture = getFixture

    fixture.db.save(any[User]) returns EitherT.fromEither[IO](().asRight[ApplicationError])
    fixture.reqRes.fetchUserData(any[Id]) returns EitherT.fromEither[IO](
      NoExternalUserError(u.id).asLeft
    )
    fixture.userService
      .create(UserCreateParams(u.id, u.email))
      .value
      .unsafeRunSync()
      .left
      .value shouldBe NoExternalUserError(u.id)
  }

  test("create user when call to reqresp service fails") {
    val u       = DataGen.randomUser
    val fixture = getFixture

    fixture.db.save(any[User]) returns EitherT.fromEither[IO](().asRight[ApplicationError])
    fixture.reqRes.fetchUserData(any[Id]) returns EitherT.fromEither[IO](
      ExternalSystemError("").asLeft
    )
    fixture.userService
      .create(UserCreateParams(u.id, u.email))
      .value
      .unsafeRunSync()
      .left
      .value shouldBe ExternalSystemError("")
  }

  test("create user that exists in database") {
    val u       = DataGen.randomUser
    val fixture = getFixture

    fixture.reqRes.fetchUserData(any[Id]) returns EitherT.fromEither[IO](
      u.into[UserData].transform.asRight
    )
    fixture.db.save(any[User]) returns EitherT.fromEither[IO](UserAlreadyRegistered(u.email).asLeft)
    fixture.userService
      .create(UserCreateParams(u.id, u.email))
      .value
      .unsafeRunSync()
      .left
      .value shouldBe UserAlreadyRegistered(u.email)
  }

}
