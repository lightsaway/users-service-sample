package vidIq.reqres.persistance

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import doobie.util.transactor.Transactor
import vidIq.reqres.domain.{
  ApplicationError,
  DatabaseError,
  EffectSyntax,
  User,
  UserAlreadyRegistered,
  UserNotFoundError
}
import doobie.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.postgres.sqlstate
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import vidIq.reqres.domain.types.{Email, Result}
sealed trait Storage[F[_]] {
  def get(email: Email): Result[F, User]
  def save(user: User): Result[F, Unit]
  def delete(email: Email): Result[F, Unit]
}

object Storage {
  def postgress[F[_]: Sync](tx: Transactor[F]) = new PostgresStorage[F](tx)

  def transactor[F[_]: Async: ContextShift](config: DbConfig): Resource[F, HikariTransactor[F]] =
    (
      for {
        ce <- ExecutionContexts.fixedThreadPool[F](config.poolSize)
        be <- Blocker[F]
        xa <- HikariTransactor.newHikariTransactor[F](
          "org.postgresql.Driver",
          config.url,
          config.username,
          config.password,
          ce,
          be
        )
      } yield xa
    ).evalTap(tx => migrate(tx))

  def migrate[F[_]: Async](transactor: HikariTransactor[F]): F[Unit] = {
    transactor.configure { dataSource =>
      Async[F].delay { Flyway.configure().dataSource(dataSource).load().migrate() }
    }
  }
}

class PostgresStorage[F[_]: Sync](tx: Transactor[F])
    extends Storage[F]
    with EffectSyntax[F, ApplicationError] {

  override def get(email: Email): Result[F, User] =
    sql"select * from users where email=${email.value}"
      .query[User]
      .unique
      .attempt
      .transact(tx)
      .leftMap(_ => UserNotFoundError(email))
      .toEitherT

  override def delete(email: Email):  Result[F, Unit] =
    sql"delete  from users where email=${email}".update.run.attempt
      .map {
        case Right(1) => ().asRight[ApplicationError]
        case _        => Left(UserNotFoundError(email))
      }
      .transact(tx)
      .toEitherT

  override def save(user: User):  Result[F, Unit] =
    sql"""insert into users (id, email, first_name, last_name) values (${user.id}, ${user.email},${user.firstName},${user.lastName})""".update.run.attemptSqlState
      .map {
        case Left(sqlstate.class23.UNIQUE_VIOLATION) =>
          UserAlreadyRegistered(user.email).asLeft[Unit]
        case Left(_)  => DatabaseError("Unexpected error appeared").asLeft[Unit]
        case Right(_) => ().asRight[ApplicationError]
      }
      .transact(tx)
      .toEitherT
}
