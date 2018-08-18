package db

import org.joda.time.LocalDate
import anorm._
import anorm.JodaParameterMetaData._
import anorm.Macro.ColumnNaming
import javax.inject.{Inject, Singleton}
import models.TaskForm
import play.api.Logger
import play.api.db.Database

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
      SQL(SelectQuery + " WHERE due_date in ({dates})")
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
    * Deletes a task given an id
    */
  def deleteById(id: Long): Unit = {
    database.withConnection { implicit c =>
      SQL(DeleteQuery)
        .on('id -> id)
        .execute()
    }
  }

  private val parser: RowParser[Task] = Macro.namedParser[Task](ColumnNaming.SnakeCase)

}
