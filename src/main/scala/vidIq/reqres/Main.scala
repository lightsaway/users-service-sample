package vidIq.reqres

import java.io.{File, FileInputStream, InputStreamReader}

import cats.effect.{ExitCode, IO, IOApp}
import io.circe.yaml.parser
import vidIq.reqres.domain.ApplicationError
import vidIq.reqres.routing.{HttpErrorHandler}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) = {

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
