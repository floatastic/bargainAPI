import akka.http.scaladsl.model.{ContentType, HttpCharsets, MediaType}

package object api {
  object ContentTypes {
    val `application/vnd.error+json` = ContentType(MediaType.applicationWithFixedCharset("vnd.error+json", HttpCharsets.`UTF-8`))
  }
}
