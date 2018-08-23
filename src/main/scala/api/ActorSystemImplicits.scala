package api

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object ActorSystemImplicits {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
}
