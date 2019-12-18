package vidIq.reqres.persistance

import cats.effect.IO
import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}

abstract trait PostgresBaseTest extends org.scalatest.FunSuite with ForAllTestContainer {
  implicit val contextShift =
    IO.contextShift(scala.concurrent.ExecutionContext.global)
  val db                 = "eventsourcing"
  override val container = PostgreSQLContainer(databaseName = db, password = "")

  def config = DbConfig(container.jdbcUrl.toString, container.username, container.password, 1)
  def tx     = Storage.transactor[IO](config).map(new PostgresStorage[IO](_))

}
