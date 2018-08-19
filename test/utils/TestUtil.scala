package utils

import java.util.UUID
import java.util.concurrent.TimeUnit

import anorm.SQL
import db.{Task, TasksDao}
import models.TaskForm
import play.api.db.Database
import play.api.libs.ws.WSClient
import play.api.{Application, inject}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, Future}

trait TestUtil {

  def injector(implicit application: Application): inject.Injector = {
    application.injector
  }

  def database(implicit application: Application): Database = injector.instanceOf[Database]

  def tasksDao(implicit application: Application): TasksDao = injector.instanceOf[TasksDao]

  def wsClient(implicit application: Application): WSClient = injector.instanceOf[WSClient]

  def clock(implicit application: Application): MockClock = injector.instanceOf[MockClock]

  // waits 10 seconds for the future to complete
  def await[T](future: Future[T]): T = {
    Await.result(future, FiniteDuration(10, TimeUnit.SECONDS))
  }

  def rightOrError[T](either: Either[_, T]): T = {
    either match {
      case Right(value) => value
      case Left(_) => throw new RuntimeException("Expected either to be right, but was left")
    }
  }

  def cleanupDb()(implicit application: Application): Unit = {
    database.withConnection { implicit c =>
      SQL("truncate tasks").execute()
    }
  }

  // validates that task and task form have the same values (with the exception of id which task form doesn't have)
  def taskEqualsTaskForm(task: Task, taskForm: TaskForm): Boolean = {
    task.name == taskForm.name &&
    task.description == taskForm.description &&
    task.dueDate == taskForm.dueDate &&
    task.completedAt == taskForm.completedAt
  }

  def randomString: String = UUID.randomUUID().toString.replace("-", "")

}
