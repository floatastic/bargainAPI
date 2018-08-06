package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class UploadSimulation extends Simulation {

  import UploadSimulation._

  val httpConf = http
    .baseURL("http://0.0.0.0:9001/")

  val scn = scenario("Upload 1 MB file to S3")
//    .exec(uploadFileRequest("v1/lots/thumbnailtmpinmem", mb1FileName)
//    .pause(30)
    .exec(uploadFileRequest(alpakkaUploadPath, mb1FileName))
    .pause(30)
    .exec(uploadFileRequest(tmpFileStreamPath, mb1FileName))

  setUp(scn.inject(rampUsers(4) over (30 seconds)).protocols(httpConf))


  def uploadFileRequest(path: String, fileName: String) = http(s"UploadFile size: $fileName, endpoint: $path")
    .post(path)
    .formUpload("file", fileName)
}

object UploadSimulation {
  val alpakkaUploadPath = "v1/lots/thumbnailalpakka"
  val tmpFileToMemPath = "v1/lots/thumbnailtmpinmem"
  val tmpFileStreamPath = "v1/lots/thumbnailtmpstream"

  val kb100FileName = "100kb"
  val kb512FileName = "512kb"
  val mb1FileName = "1mb"
}
