package vidIq.reqres.services

import cats.effect.Sync
import vidIq.reqres.domain.User
import vidIq.reqres.domain.types.{Email, Id, Result}
import vidIq.reqres.persistance.Storage

trait UserService[F[_]] {
  def get(email: Email): Result[F, User]
  def delete(email: Email): Result[F, Unit]
  def create(p: UserCreateParams): Result[F, User]
}

case class UserCreateParams(id: Id, email: Email)

class UserServiceImpl[F[_]: Sync](storage: Storage[F], reqRes: ReqResService[F])
    extends UserService[F] {

  override def get(email: Email): Result[F, User] = storage.get(email)

  override def create(p: UserCreateParams):  Result[F, User]  =
    for {
      userData <- reqRes.fetchUserData(p.id)
      user = User(p.id, p.email, userData.firstName, userData.lastName)
      _ <- storage.save(user)
    } yield user

  override def delete(email: Email): Result[F, Unit]  = storage.delete(email)

}
