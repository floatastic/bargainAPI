package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UploadSimulation extends Simulation {

  import UploadSimulation._

  val httpConf = http
    .baseURL("http://0.0.0.0:9001/")

  val scenarioAlpakka = scenario("Upload 1 MB file to S3 with alpakka")
    .exec(uploadFileRequest(alpakkaUploadPath, mb1FileName))

  val scenarioTmpFile = scenario("Upload 1 MB file to S3 with tmp file")
    .exec(uploadFileRequest(tmpFilePath, mb1FileName))

  setUp(
    scenarioTmpFile.inject(
      splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
    ).protocols(httpConf),

    scenarioAlpakka.inject(
      splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
    ).protocols(httpConf)
  )


  def uploadFileRequest(path: String, fileName: String) = http(s"UploadFile size: $fileName, endpoint: $path")
    .post(path)
    .formUpload("file", fileName)
}

object UploadSimulation {
  val alpakkaUploadPath = "v1/lots/thumbnailalpakka"
  val tmpFilePath = "v1/lots/thumbnailtmpfile"

  val kb100FileName = "100kb"
  val kb512FileName = "512kb"
  val mb1FileName = "1mb"
}
