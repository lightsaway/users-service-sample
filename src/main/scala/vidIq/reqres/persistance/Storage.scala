package vidIq.reqres.persistance

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import cats.effect.concurrent.Ref
import cats.implicits._
import doobie.util.transactor.Transactor
import vidIq.reqres.domain.{DatabaseError, User, UserAlreadyRegistered, UserNotFoundError}
import doobie.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.postgres.sqlstate
import doobie.util.ExecutionContexts
import org.flywaydb.core.Flyway
import vidIq.reqres.domain.types.Email

sealed trait Storage[F[_]] {
  def get(email: Email): F[User]
  def save(user: User): F[Unit]
  def delete(email: Email): F[Unit]
}

object Storage {
  def inMemory[F[_]: Sync](
      state: Map[Email, User] = Map.empty[Email, User]
  ): F[Storage[F]] =
    Ref
      .of[F, Map[Email, User]](state)
      .map(new InMemoryStorage[F](_))
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

class InMemoryStorage[F[_]](R: Ref[F, Map[Email, User]])(implicit F: Sync[F]) extends Storage[F] {
  override def get(email: Email) = R.get.map(_.get(email)).flatMap {
    case None    => F.raiseError[User](UserNotFoundError(email))
    case Some(u) => F.delay(u)
  }

  override def delete(email: Email) = R.get.map(_.get(email)).flatMap {
    case None    => F.raiseError(UserNotFoundError(email))
    case Some(_) => R.update(m => m - email)
  }

  override def save(user: User): F[Unit] = R.get.map(_.get(user.email)).flatMap {
    case Some(_) => F.raiseError(UserAlreadyRegistered(user.email))
    case None    => R.update(m => m + (user.email -> user))
  }

}

class PostgresStorage[F[_]: Sync](tx: Transactor[F]) extends Storage[F] {

  override def get(email: Email) =
    sql"select * from users where email=${email.value}"
      .query[User]
      .option
      .flatMap {
        case Some(item) => FC.pure(item)
        case None       => FC.raiseError[User](UserNotFoundError(email))
      }
      .transact(tx)

  override def delete(email: Email) =
    sql"delete  from users where email=${email}".update.run
      .flatMap {
        case 1 => FC.unit
        case _ => FC.raiseError[Unit](UserNotFoundError(email))
      }
      .transact(tx)

  override def save(user: User) =
    sql"""insert into users (id, email, first_name, last_name) values (${user.id}, ${user.email},${user.firstName},${user.lastName})""".update.run
      .exceptSomeSqlState {
        case sqlstate.class23.UNIQUE_VIOLATION => FC.raiseError(UserAlreadyRegistered(user.email))
        case _                                 => FC.raiseError(DatabaseError("Unexpected error appeared"))
      }
      .transact(tx)
      .void
}
