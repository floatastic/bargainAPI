package api

import java.io.File

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.event.{ProgressEvent, ProgressEventType, ProgressListener}
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import config.Config

import scala.concurrent.{Future, Promise}

case class S3UploaderException(msg: String) extends Exception(msg)

object S3Uploader extends Config {
  def upload(file: File, key: String): Future[String] = {
    val credentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val s3Client = new AmazonS3Client(credentials)
    s3Client.setRegion(Region.getRegion(Regions.EU_WEST_3))

    val listener = new ProgressListener() {
      val promise = Promise[String]()

      override def progressChanged(progressEvent: ProgressEvent): Unit = {
        progressEvent.getEventType match {
          case ProgressEventType.TRANSFER_FAILED_EVENT => promise.failure(S3UploaderException(s"Uploading a file with a key: $key"))
          case ProgressEventType.TRANSFER_COMPLETED_EVENT => promise.success(key)
        }
      }
    }

    val request = new PutObjectRequest(s3Bucket, key, file)
    request.setGeneralProgressListener(listener)

    s3Client.putObject(request)

    listener.promise.future
  }
}
