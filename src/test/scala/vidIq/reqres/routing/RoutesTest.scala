package vidIq.reqres.routing

import cats.data.EitherT
import cats.effect.IO
import cats.implicits._
import io.circe.generic.semiauto.deriveEncoder
import io.circe.literal._
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.{EntityEncoder, Method, Request, Uri}
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.{FunSuite, Matchers}
import vidIq.reqres.DataGen
import vidIq.reqres.domain.types.{Email, Id}
import vidIq.reqres.domain.{ExternalSystemError, UserAlreadyRegistered, UserNotFoundError}
import vidIq.reqres.services.{UserCreateParams, UserService}

class RoutesTest extends FunSuite with Matchers with IdiomaticMockito with ArgumentMatchersSugar {

  implicit val encoder: Encoder[UserCreateRequest] = deriveEncoder
  implicit val entityEncoder: EntityEncoder[IO, UserCreateRequest] =
    jsonEncoderOf[IO, UserCreateRequest]

  test("get user") {
    val service = mock[UserService[IO]]
    val user    = DataGen.staticUser
    service.get(any[Email]) returns EitherT.fromEither[IO](Right(user))
    val req = Request[IO](uri = Uri(path = s"/users/${user.email}"))

    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 200

    val expected = json"""{
          "id" : 1,
          "email" : "first@gmail.com",
          "firstName" : "Man",
          "lastName" : "Hero"
      }"""

    res.as[Json].unsafeRunSync() shouldBe expected

  }

  test("get not existing user") {
    val user    = DataGen.staticUser
    val service = mock[UserService[IO]]
    service.get(any[Email]) returns EitherT.fromEither[IO](UserNotFoundError(user.email).asLeft)

    val req = Request[IO](uri = Uri(path = s"/users/${user.email.value}"))
    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get

    res.status.code shouldBe 404
    val expected = json"""{
              "message" : "No user found with email first@gmail.com"
          }"""
    res.as[Json].unsafeRunSync() shouldBe expected

  }

  test("create  user") {
    val service = mock[UserService[IO]]
    val user    = DataGen.staticUser
    service.create(any[UserCreateParams]) returns EitherT.fromEither[IO](Right(user))
    val req = Request[IO](method = Method.POST, uri = Uri(path = s"/users"))
      .withEntity(UserCreateRequest(user.id, user.email))

    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 200

    val expected = json"""{
                  "id" : 1,
                  "email" : "first@gmail.com",
                  "firstName" : "Man",
                  "lastName" : "Hero"
                }"""

    res.as[Json].unsafeRunSync() shouldBe expected
  }

  test("create duplicate user") {
    val service = mock[UserService[IO]]
    val user    = DataGen.staticUser
    service.create(any[UserCreateParams]) returns EitherT.fromEither[IO](
      UserAlreadyRegistered(user.email).asLeft
    )
    val req = Request[IO](method = Method.POST, uri = Uri(path = s"/users"))
      .withEntity(UserCreateRequest(Id(1), Email("some@email.com")))

    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 409

    val expected = json"""{
            "message" : "User already exists with email first@gmail.com"
    }"""

    res.as[Json].unsafeRunSync() shouldBe expected

  }

  test("delete not existing user") {
    val user = DataGen.staticUser
    val req  = Request[IO](method = Method.DELETE, uri = Uri(path = s"/users/${user.email}"))

    val service = mock[UserService[IO]]
    service.delete(any[Email]) returns EitherT.fromEither[IO](UserNotFoundError(user.email).asLeft)

    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 404
    val expected = json"""{
              "message" : "No user found with email first@gmail.com"
          }"""
    res.as[Json].unsafeRunSync() shouldBe expected
  }

  test("delete created user") {
    val user = DataGen.staticUser
    val req  = Request[IO](method = Method.DELETE, uri = Uri(path = s"/users/${user.email}"))

    val service = mock[UserService[IO]]
    service.delete(any[Email]) returns EitherT.fromEither[IO](().asRight)

    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 204
  }

  test("some unexpected error") {
    val user = DataGen.staticUser
    val req  = Request[IO](method = Method.DELETE, uri = Uri(path = s"/users/${user.email}"))

    val service = mock[UserService[IO]]
    service.delete(any[Email]) returns EitherT.fromEither[IO](ExternalSystemError("foo").asLeft)

    val res = new Routes[IO].userRoutes(service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 500
    val expected = json"""{
          "message" : "Having problems with integrated services"
      }"""
    res.as[Json].unsafeRunSync() shouldBe expected
  }
}
