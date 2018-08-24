package db

import anorm.JodaParameterMetaData._
import anorm.Macro.ColumnNaming
import anorm._
import db.TasksDao.parser
import javax.inject.{Inject, Singleton}
import models.TaskForm
import org.joda.time.LocalDate
import play.api.Logger
import play.api.db.Database
import play.api.libs.json._

case class Task(
  id: Long,
  name: String,
  description: String,
  dueDate: LocalDate,
  completedAt: Option[LocalDate]
)

@Singleton
class TasksDao @Inject() (
  database: Database
) {

  private val InsertQuery: String =
    """
      | INSERT INTO tasks(name, description, due_date, completed_at) VALUES ({name}, {description}, {due_date}, {completed_at})
    """.stripMargin

  private val UpdateQuery: String =
    """
      | UPDATE tasks set name = {name}, description = {description}, due_date = {due_date}, completed_at = {completed_at} where id = {id}
    """.stripMargin
  private val SelectQuery: String =
    """
      | SELECT * FROM tasks
    """.stripMargin

  private val DeleteQuery: String =
    """
      | DELETE from tasks where id = {id}
    """.stripMargin

  /**
    * Inserts task form into the database
    * @return On error returns left with error message. On success returns right with Task created
    */
  def insert(form: TaskForm): Either[String, Task] = {
    database.withConnection { implicit c =>
      SQL(InsertQuery).on(
        'name -> form.name,
        'description -> form.description,
        'due_date -> form.dueDate,
        'completed_at -> form.completedAt
      ).executeInsert[Option[Long]]() match {
        case Some(id) => findById(id) match {
          case Some(task) => Right(task)
          case None =>
            Logger.error(s"TasksDao - Failed to insert form: [$form]")
            Left("Failed to insert task form")

        }
        case None =>
          Logger.error(s"TasksDao - Failed to insert form: [$form]")
          Left("Failed to insert task form")
      }
    }
  }

  /**
    * Finds all tasks
    */
  def findAll(): Seq[Task] = {
    database.withConnection { implicit c =>
      SQL(SelectQuery)
        .as(parser.*)
    }
  }

  /**
    * Finds a task by id
    */
  def findById(id: Long): Option[Task] = {
    database.withConnection { implicit c =>
      SQL(SelectQuery + " WHERE id = {id}")
        .on('id -> id)
        .as(parser.*)
        .headOption
    }
  }

  /**
    * Finds all tasks that have due dates in the given dates
    */
  def findByDueDate(dates: Seq[LocalDate]): Seq[Task] = {
    database.withConnection { implicit c =>
      SQL(SelectQuery + " WHERE due_date in ({dates}) and completed_at is null")
        .on('dates -> dates)
        .as(parser.*)
    }
  }

  /**
    * Finds all completed tasks
    */
  def findAllCompleted(): Seq[Task] = {
    database.withConnection { implicit c =>
      SQL(SelectQuery + " WHERE completed_at is not null")
        .as(parser.*)
    }
  }

  /**
    * Find overdue tasks using the input date as the current date
    */
  def findOverdue(date: LocalDate): Seq[Task] = {
    database.withConnection { implicit c =>
      SQL(SelectQuery + " WHERE completed_at is null and due_date < {date}")
        .on('date -> date)
        .as(parser.*)
    }
  }

  /**
    * Updates the existing record if it exists
    */
  def updateById(id: Long, taskForm: TaskForm): Option[Task] = {
    database.withConnection { implicit c =>
      SQL(UpdateQuery)
        .on('id -> id)
        .on('name -> taskForm.name)
        .on('description -> taskForm.description)
        .on('due_date -> taskForm.dueDate)
        .on('completed_at -> taskForm.completedAt)
        .executeUpdate()

      findById(id)
    }
  }

  /**
    * Deletes a task given an id
    */
  def deleteById(id: Long): Unit = {
    database.withConnection { implicit c =>
      SQL(DeleteQuery)
        .on('id -> id)
        .execute()
    }
  }

}


object TasksDao {

  import play.api.libs.functional.syntax._
  import utils.LocalDateUtil._

  private val parser: RowParser[Task] = Macro.namedParser[Task](ColumnNaming.SnakeCase)

  implicit val TaskWrites: Writes[Task] = new Writes[Task] {
    def writes(task: Task): JsObject = Json.obj(
      "id" -> task.id,
      "name" -> task.name,
      "description" -> task.description,
      "due_date" -> task.dueDate.toString(),
      "completed_at" -> task.completedAt.map(_.toString())
    )
  }

  implicit val TaskReads: Reads[Task] = (
    (JsPath \ "id").read[Long] and
    (JsPath \ "name").read[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "due_date").read[LocalDate] and
    (JsPath \ "completed_at").readNullable[LocalDate]
    )(Task.apply _)

}
