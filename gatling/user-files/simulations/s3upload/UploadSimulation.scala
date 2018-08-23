package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class UploadSimulation extends Simulation {

  import UploadSimulation._

  val httpConf = http
    .baseURL("http://0.0.0.0:9001/")

  val scenarioAlpakka = scenario("Upload 10kb file to S3 with alpakka")
    .exec(uploadFileRequest(alpakkaUploadPath, kb10FileName))

  val scenarioTmpFile = scenario("Upload 10kb file to S3 with tmp file")
    .exec(uploadFileRequest(tmpFilePath, kb10FileName))

  setUp(
//    scenarioTmpFile.inject(
//      atOnceUsers(1000)
//      //splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
//    ).protocols(httpConf),

    scenarioAlpakka.inject(
      atOnceUsers(1000)
      //splitUsers(50) into (atOnceUsers(5)) separatedBy (30 seconds)
    ).protocols(httpConf)
  )


  def uploadFileRequest(path: String, fileName: String) = http(s"UploadFile size: $fileName, endpoint: $path")
    .post(path)
    .formUpload("file", fileName)
}

object UploadSimulation {
  val alpakkaUploadPath = "v1/uploadtest/alpakka"
  val tmpFilePath = "v1/uploadtest/tmpfile"

  val kb10FileName = "10kb"
}
