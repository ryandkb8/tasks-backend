package controllers

import akka.actor.ActorSystem
import db.{Task, TasksDao}
import db.TasksDao._
import javax.inject.{Inject, Singleton}
import models.TaskForm
import models.TaskForm._
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc._
import utils.Clock

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TasksController @Inject()(
  val controllerComponents: ControllerComponents,
  tasksDao: TasksDao,
  system: ActorSystem,
  clock: Clock
) extends BaseController {

  implicit val ec: ExecutionContext = system.dispatchers.lookup("tasks-context")

  def getAll(
    dueToday: Boolean = false,
    dueTomorrow: Boolean = false,
    overdue: Boolean = false,
    completed: Boolean = false
  ): Action[AnyContent] = Action.async {
    Future {
      val today = clock.currentDate
      if (dueToday || dueTomorrow) {
        val todayDate = if (dueToday) Some(today) else None
        val tomorrowDate = if (dueTomorrow) Some(today.plusDays(1)) else None
        val dates = Seq(todayDate, tomorrowDate).flatten
        Ok(Json.toJson(tasksDao.findByDueDate(dates)))
      } else if (overdue) {
        Ok(Json.toJson(tasksDao.findOverdue(today)))
      } else if (completed) {
        Ok(Json.toJson(tasksDao.findAllCompleted()))
      } else {
        Ok(Json.toJson(tasksDao.findAll()))
      }
    }
  }

  def getById(id: Long): Action[AnyContent] = Action.async {
    Future {
      tasksDao.findById(id) match {
        case Some(task) => Ok(Json.toJson(task))
        case None => NotFound("")
      }
    }
  }

  def post(): Action[AnyContent] = Action.async { request =>
    Future {
      request.body.asJson.flatMap(_.asOpt[TaskForm]) match {
        case Some(form) => tasksDao.insert(form) match {
          case Right(task) => Created(Json.toJson(task))
          case Left(_) => InternalServerError("")
        }
        case None => UnprocessableEntity("")
      }
    }
  }

  def putById(id: Long): Action[AnyContent] = Action.async { request =>
    Future {
      request.body.asJson.flatMap(_.asOpt[TaskForm]) match {
        case Some(form) =>
          withTask(id, { _ =>
            tasksDao.updateById(id, form) match {
              case Some(updatedTask) => Ok(Json.toJson(updatedTask))
              case None =>
                // this should never happen if there is only one user using this
                // it could happen if we check it exists and then it gets deleted before the update is completed
                // a row lock could solve this, but lets ignore for now
                Logger.error("TasksDaoController - Task not found when updating task")
                NotFound("")
            }
          })
        case None => UnprocessableEntity("")
      }
    }
  }

  def deleteById(id: Long): Action[AnyContent] = Action.async { request =>
    Future {
      withTask(id, _ => {
        tasksDao.deleteById(id)
        Ok("")
      })
    }
  }

  private def withTask(id: Long, f: Task => Result): Result = {
    tasksDao.findById(id) match {
      case Some(task) => {
        f(task)
      }
      case None => NotFound("")
    }
  }

}
