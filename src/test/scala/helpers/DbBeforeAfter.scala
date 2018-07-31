package helpers

import db.Migrator
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite} //or fixture.suite

trait DbBeforeAfter extends Suite with BeforeAndAfterEach with BeforeAndAfterAll with Migrator with TestData  {

  override def beforeAll() {
    super.beforeAll
    migrateUp
    insertTestData
  }

  override def afterAll() {
    ensureTransaction(false)
    dropAllData
    super.afterAll
  }

  override def beforeEach = {
    super.beforeEach
    ensureTransaction(true)
  }

  override def afterEach: Unit = {
    rollback
    super.afterEach
  }
}
