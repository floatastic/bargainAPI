package config

import com.typesafe.config.ConfigFactory

trait Config {
  val config = ConfigFactory.load()

  private val httpConfig = config.getConfig("http")
  private val databaseConfig = config.getConfig("database")
  private val awsConfig = config.getConfig("aws")

  val httpInterface = httpConfig.getString("interface")
  val httpPort = httpConfig.getInt("port")

  val databaseUrl = databaseConfig.getString("url")
  val databaseUser = databaseConfig.getString("user")
  val databasePassword = databaseConfig.getString("password")

  val s3Bucket = awsConfig.getString("s3Bucket")
  val awsAccessKey = awsConfig.getString("accessKey")
  val awsSecretKey = awsConfig.getString("secretKey")
}