package api

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.server.directives.FileInfo
import akka.stream.Materializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.alpakka.s3.scaladsl.{MultipartUploadResult, S3Client}
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.event.{ProgressEvent, ProgressEventType, ProgressListener}
import com.amazonaws.regions.{AwsRegionProvider, Region, Regions}
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectRequest
import config.Config

import scala.concurrent.{Future, Promise}

case class S3UploaderException(msg: String) extends Exception(msg)

object S3Uploader extends Config with FileHelper {
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

  def sink(fileInfo: FileInfo)(implicit as: ActorSystem, m: Materializer) = {
    val awsCredentials = new BasicAWSCredentials(awsAccessKey, awsSecretKey)
    val awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCredentials)
    val regionProvider =
      new AwsRegionProvider {
        def getRegion: String = Regions.EU_WEST_3.getName
      }

    val settings = new S3Settings(MemoryBufferType, None, awsCredentialsProvider, regionProvider, false, None, ListBucketVersion2)
    val s3Client = new S3Client(settings)(as, m)

    val key = tmpFileName(fileInfo)

    s3Client.multipartUpload(s3Bucket, key._1 + key._2)
  }
}
