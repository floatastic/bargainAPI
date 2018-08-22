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
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials, DefaultAWSCredentialsProviderChain}
import com.amazonaws.event.{ProgressEvent, ProgressEventType, ProgressListener}
import com.amazonaws.regions.{AwsRegionProvider, Region, Regions}
import com.amazonaws.services.s3.{AmazonS3, AmazonS3Client, AmazonS3ClientBuilder}
import com.amazonaws.services.s3.model.PutObjectRequest
import config.Config

import scala.concurrent.{Future, Promise}

case class S3UploaderException(msg: String) extends Exception(msg)

object S3Uploader extends Config with FileHelper {
  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard()
    .withCredentials(new DefaultAWSCredentialsProviderChain())
    .withRegion(Regions.EU_WEST_3)
    .build()

  def upload(file: File, key: String): Future[String] = {
    val promise = Promise[String]()

    val listener = new ProgressListener() {
      override def progressChanged(progressEvent: ProgressEvent): Unit = {
        (progressEvent.getEventType: @unchecked) match {
          case ProgressEventType.TRANSFER_FAILED_EVENT => promise.failure(S3UploaderException(s"Uploading a file with a key: $key"))
          case ProgressEventType.TRANSFER_COMPLETED_EVENT |
               ProgressEventType.TRANSFER_CANCELED_EVENT => promise.success(key)

        }
      }
    }

    val request = new PutObjectRequest(s3Bucket, key, file)
    request.setGeneralProgressListener(listener)

    s3Client.putObject(request)

    promise.future
  }

  def sink(fileInfo: FileInfo)(implicit as: ActorSystem, m: Materializer) = {
    val regionProvider =
      new AwsRegionProvider {
        def getRegion: String = Regions.EU_WEST_3.getName
      }

    val settings = new S3Settings(MemoryBufferType, None, new DefaultAWSCredentialsProviderChain(), regionProvider, false, None, ListBucketVersion2)
    val s3Client = new S3Client(settings)(as, m)

    val key = tmpFileName(fileInfo)

    s3Client.multipartUpload(s3Bucket, key._1 + key._2)
  }
}
