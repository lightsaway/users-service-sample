package vidIq.reqres.routing

import cats.effect.IO
import cats.implicits._
import com.olegpy.meow.hierarchy._
import io.circe.{Encoder, Json}
import org.http4s.circe._
import org.http4s.{EntityEncoder, Method, Request, Uri}
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.scalatest.{FunSuite, Matchers}
import vidIq.reqres.DataGen
import vidIq.reqres.domain.{
  ApplicationError,
  ExternalSystemError,
  User,
  UserAlreadyRegistered,
  UserNotFoundError
}
import vidIq.reqres.services.{UserCreateParams, UserService}
import io.circe.literal._
import io.circe.generic.semiauto.deriveEncoder
import vidIq.reqres.domain.types.{Email, Id}

class RoutesTest extends FunSuite with Matchers with IdiomaticMockito with ArgumentMatchersSugar {

  implicit val userHttpErrorHandler: HttpErrorHandler[IO, ApplicationError] =
    new UserHttpErrorHandler[IO]

  implicit val encoder: Encoder[UserCreateRequest] = deriveEncoder
  implicit val entityEncoder: EntityEncoder[IO, UserCreateRequest] =
    jsonEncoderOf[IO, UserCreateRequest]

  test("get user") {
    val service = mock[UserService[IO]]
    val user    = DataGen.staticUser
    service.get(any[Email]) returns user.pure[IO]
    val req = Request[IO](uri = Uri(path = s"/users/${user.email}"))

    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get
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
    service.get(any[Email]) returns IO.raiseError(UserNotFoundError(user.email))

    val req = Request[IO](uri = Uri(path = s"/users/${user.email.value}"))
    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get

    res.status.code shouldBe 404
    val expected = json"""{
              "message" : "No user found with email first@gmail.com"
          }"""
    res.as[Json].unsafeRunSync() shouldBe expected

  }

  test("create  user") {
    val service = mock[UserService[IO]]
    val user    = DataGen.staticUser
    service.create(any[UserCreateParams]) returns user.pure[IO]
    val req = Request[IO](method = Method.POST, uri = Uri(path = s"/users"))
      .withEntity(UserCreateRequest(user.id, user.email))

    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get
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
    service.create(any[UserCreateParams]) returns IO.raiseError(UserAlreadyRegistered(user.email))
    val req = Request[IO](method = Method.POST, uri = Uri(path = s"/users"))
      .withEntity(UserCreateRequest(Id(1), Email("some@email.com")))

    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get
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
    service.delete(any[Email]) returns IO.raiseError[Unit](UserNotFoundError(user.email))

    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get
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
    service.delete(any[Email]) returns IO.unit

    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 204
  }

  test("some unexpected error") {
    val user = DataGen.staticUser
    val req  = Request[IO](method = Method.DELETE, uri = Uri(path = s"/users/${user.email}"))

    val service = mock[UserService[IO]]
    service.delete(any[Email]) returns IO.raiseError[Unit](ExternalSystemError("foo"))

    val res = Routes.userRoutes[IO](service).run(req).orElse(fail()).value.unsafeRunSync().get
    res.status.code shouldBe 500
    val expected = json"""{
          "message" : "Having problems with integrated services"
      }"""
    res.as[Json].unsafeRunSync() shouldBe expected
  }
}
