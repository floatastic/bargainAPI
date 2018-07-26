package db

import scala.slick.driver.PostgresDriver.simple._

trait Database {
  val driver = slick.driver.PostgresDriver

  //import driver.api._

  def db = Database.forConfig("database")

  implicit val session: Session = db.createSession()
}
