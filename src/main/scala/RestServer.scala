import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import config.Config
import db.Migrator

import scala.io.StdIn

object RestServer extends App with Config with Routes with Migrator {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  protected val log: LoggingAdapter = Logging(system, getClass)

  migrateUp

  val bindingFuture = Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)

  println(s"Server online at http://$httpInterface:$httpPort/\nPress RETURN to stop...")
  StdIn.readLine()

  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
