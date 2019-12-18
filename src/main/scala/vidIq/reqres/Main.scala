package vidIq.reqres

import java.io.{File, FileInputStream, InputStreamReader}

import cats.effect.{ExitCode, IO, IOApp}
import io.circe.yaml.parser
import vidIq.reqres.domain.ApplicationError
import vidIq.reqres.routing.{HttpErrorHandler, UserHttpErrorHandler}
import cats.implicits._
import com.olegpy.meow.hierarchy._

object Main extends IOApp {
  def run(args: List[String]) = {
    implicit val userHttpErrorHandler: HttpErrorHandler[IO, ApplicationError] =
      new UserHttpErrorHandler[IO]
    Server
      .stream[IO](
        parseConfig(
          args.headOption.getOrElse(throw new IllegalArgumentException("need to supply config"))
        )
      )
      .compile
      .drain
      .as(ExitCode.Success)
  }

  private def parseConfig(fileName: String): Config = {
    parser
      .parse(new InputStreamReader(new FileInputStream(new File(fileName))))
      .flatMap(_.as[Config])
      .valueOr(throw _)
  }
}
