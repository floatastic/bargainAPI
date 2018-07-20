import akka.http.scaladsl.model.{ContentType, HttpCharsets, MediaType}

package object api {
  object ContentTypes {
    val `application/vnd.api+json` = ContentType(MediaType.applicationWithFixedCharset("vnd.api+json", HttpCharsets.`UTF-8`))
  }
}
