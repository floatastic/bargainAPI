package db

import config.Config
import org.flywaydb.core.Flyway

trait Migrator extends Config {
  private val flyway = new Flyway()
  flyway.setDataSource(databaseUrl, databaseUser, databasePassword)

  def migrateUp = flyway.migrate()

  def migrateDownAll = flyway.clean()
}
