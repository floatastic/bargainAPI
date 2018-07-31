package db

import com.mchange.v2.c3p0.ComboPooledDataSource
import config.Config

import scala.slick.driver.PostgresDriver.simple._

trait Database extends Config {
  val driver = slick.driver.PostgresDriver

  val datasource = new ComboPooledDataSource
  datasource.setDriverClass("org.postgresql.Driver")
  datasource.setJdbcUrl(databaseUrl)
  datasource.setUser(databaseUser)
  datasource.setPassword(databasePassword)

  def db = Database.forDataSource(datasource)

  implicit val session: Session = db.createSession()

}
