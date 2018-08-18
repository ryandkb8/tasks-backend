package utils

import java.util.UUID

import db.TasksDao
import play.api.db.Database
import play.api.{Application, inject}

trait TestUtil {

  def injector(implicit application: Application): inject.Injector = {
    application.injector
  }

  def database(implicit application: Application): Database = injector.instanceOf[Database]

  def tasksDao(implicit application: Application): TasksDao = injector.instanceOf[TasksDao]

  def randomString: String = UUID.randomUUID().toString.replace("-", "")

}
