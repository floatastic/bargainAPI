package db

import config.Config
import org.flywaydb.core.Flyway

trait Migrator extends Config {
  private val flyway = new Flyway()
  flyway.setDataSource(databaseUrl, databaseUser, databasePassword)

  def migrate = flyway.migrate()

  def rollback = flyway.clean()
}
