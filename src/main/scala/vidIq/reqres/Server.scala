package vidIq.reqres

import cats.effect.{ConcurrentEffect, ContextShift, Sync, Timer}
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.client.middleware.{Logger => Clogger}
import org.http4s.implicits._
import org.http4s.server.Router
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import vidIq.reqres.domain.ApplicationError
import vidIq.reqres.persistance.Storage
import vidIq.reqres.routing.{HttpErrorHandler, Routes}
import vidIq.reqres.services.{ReqResServiceImpl, UserServiceImpl}

import scala.concurrent.ExecutionContext.global

object Server {

  def stream[F[_]: ConcurrentEffect: Sync](
      config: Config
  )(
      implicit T: Timer[F],
      C: ContextShift[F],
      H: HttpErrorHandler[F, ApplicationError]
  ): Stream[F, Nothing] = {
    for {
      client <- BlazeClientBuilder[F](global).stream
      tx     <- Stream.resource(Storage.transactor(config.dbConfig))
      loggingClient = Clogger(logHeaders = true, logBody = true)(client)
      storage <- Stream.eval(Storage.postgress(tx).pure[F])
      reqRes       = new ReqResServiceImpl[F](config.integration.uri, loggingClient)
      service      = new UserServiceImpl[F](storage, reqRes)
      router       = Router("/api/v1" -> Routes.userRoutes(service)).orNotFound
      finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(router)
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain

}
