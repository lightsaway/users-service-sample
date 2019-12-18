package vidIq.reqres.services

import cats.effect.Sync
import cats.implicits._
import vidIq.reqres.domain.User
import vidIq.reqres.domain.types.{Email, Id}
import vidIq.reqres.persistance.Storage

trait UserService[F[_]] {
  def get(email: Email): F[User]
  def delete(email: Email): F[Unit]
  def create(p: UserCreateParams): F[User]
}

case class UserCreateParams(id: Id, email: Email)

class UserServiceImpl[F[_]: Sync](storage: Storage[F], reqRes: ReqResService[F])
    extends UserService[F] {

  override def get(email: Email) = storage.get(email)

  override def create(p: UserCreateParams) =
    for {
      userData <- reqRes.fetchUserData(p.id)
      user = User(p.id, p.email, userData.firstName, userData.lastName)
      _ <- storage.save(user)
    } yield user

  override def delete(email: Email) = storage.delete(email)

}
