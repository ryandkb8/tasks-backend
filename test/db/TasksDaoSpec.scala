package db

import anorm.SQL
import models.TaskForm
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.TestUtil

class TasksDaoSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter with TestUtil {

  // before each test runs delete everything from the database
  before {
    database.withConnection { implicit c =>
      SQL("truncate tasks").execute()
    }
  }

  "insert into tasks with empty completed at" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None)
    val taskEither = tasksDao.insert(taskForm)
    val task = rightOrFail(taskEither)
    taskEqualsTaskForm(task, taskForm)
  }

  "insert into tasks when completed at is not empty" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))
    val taskEither = tasksDao.insert(taskForm)
    val task = rightOrFail(taskEither)
    taskEqualsTaskForm(task, taskForm)
  }

  "find tasks by id" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None)
    val taskEither = tasksDao.insert(taskForm)
    val task = rightOrFail(taskEither)

    val foundTask = tasksDao.findById(task.id).getOrElse(fail(s"Could not find task for id: ${task.id}"))
    foundTask mustBe task
  }

  "find tasks by id when id doesn't exist" in {
    tasksDao.findById(Long.MaxValue).isDefined mustBe false
  }

  "find all tasks" in {
    val taskForm1 = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))
    val taskForm2 = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))
    val taskForm3 = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))

    val task1 = rightOrFail(tasksDao.insert(taskForm1))
    val task2 = rightOrFail(tasksDao.insert(taskForm2))
    val task3 = rightOrFail(tasksDao.insert(taskForm3))

    val tasks = tasksDao.findAll()
    tasks.size mustBe 3
    tasks.exists(_.id == task1.id) mustBe true
    tasks.exists(_.id == task2.id) mustBe true
    tasks.exists(_.id == task3.id) mustBe true
  }

  "find all tasks when there are none" in {
    tasksDao.findAll().size mustBe 0
  }

  "find all tasks due in specific dates" in {
    val date1 = new LocalDate(2018, 8, 18)
    val date2 = new LocalDate(2018, 8, 20)
    val date3 = new LocalDate(2018, 8, 21)
    val taskForm1 = TaskForm(randomString, "My first task", date1, Some(new LocalDate(2018, 8, 20)))
    val taskForm2 = TaskForm(randomString, "My first task", date1, Some(new LocalDate(2018, 8, 20)))
    val taskForm3 = TaskForm(randomString, "My first task", date2, Some(new LocalDate(2018, 8, 20)))
    val taskForm4 = TaskForm(randomString, "My first task", date3, Some(new LocalDate(2018, 8, 20)))

    val task1 = rightOrFail(tasksDao.insert(taskForm1))
    val task2 = rightOrFail(tasksDao.insert(taskForm2))
    val task3 = rightOrFail(tasksDao.insert(taskForm3))
    val task4 = rightOrFail(tasksDao.insert(taskForm4))

    val tasks = tasksDao.findByDueDate(Seq(date1, date3))
    tasks.size mustBe 3
    tasks.exists(_.id == task1.id) mustBe true
    tasks.exists(_.id == task2.id) mustBe true
    tasks.exists(_.id == task4.id) mustBe true
  }

  "find all completed tasks" in {
    val dueDate = new LocalDate(2018, 8, 18)
    val completedDate = new LocalDate(2018, 8, 20)
    val taskForm1 = TaskForm(randomString, "My first task", dueDate, Some(completedDate))
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm3 = TaskForm(randomString, "My first task", dueDate, Some(completedDate))
    val taskForm4 = TaskForm(randomString, "My first task", dueDate, None)

    val task1 = rightOrFail(tasksDao.insert(taskForm1))
    val task2 = rightOrFail(tasksDao.insert(taskForm2))
    val task3 = rightOrFail(tasksDao.insert(taskForm3))
    val task4 = rightOrFail(tasksDao.insert(taskForm4))

    val tasks = tasksDao.findAllCompleted()
    tasks.size mustBe 2
    tasks.exists(_.id == task1.id) mustBe true
    tasks.exists(_.id == task3.id) mustBe true
  }

  "find all overdue tasks" in {
    val dueDate = new LocalDate(2018, 8, 18)
    val dueDate2 = new LocalDate(2018, 8, 19)
    val completedDate = new LocalDate(2018, 8, 20)
    val taskForm1 = TaskForm(randomString, "My first task", dueDate, Some(completedDate))
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm3 = TaskForm(randomString, "My first task", dueDate, Some(completedDate))
    val taskForm4 = TaskForm(randomString, "My first task", dueDate2, None)

    val task1 = rightOrFail(tasksDao.insert(taskForm1))
    val task2 = rightOrFail(tasksDao.insert(taskForm2))
    val task3 = rightOrFail(tasksDao.insert(taskForm3))
    val task4 = rightOrFail(tasksDao.insert(taskForm4))

    // this should find task2 and task4 as they were due the 18th and were passing in the 20th
    val tasks = tasksDao.findOverdue(new LocalDate(2018, 8, 20))
    tasks.size mustBe 2
    tasks.exists(_.id == task2.id) mustBe true
    tasks.exists(_.id == task4.id) mustBe true

    // this should find task2 as it was due the 18th and were passing in the 19th
    // this shouldn't find task4 as that's due the 19th so its not yet overdue
    val tasks2 = tasksDao.findOverdue(new LocalDate(2018, 8, 19))
    tasks2.size mustBe 1
    tasks2.exists(_.id == task2.id) mustBe true
  }

  "delete task" in {
    val taskForm1 = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))
    val taskForm2 = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))

    val task1 = rightOrFail(tasksDao.insert(taskForm1))
    val task2 = rightOrFail(tasksDao.insert(taskForm2))

    val tasks = tasksDao.findAll()
    tasks.size mustBe 2
    tasks.exists(_.id == task1.id) mustBe true
    tasks.exists(_.id == task2.id) mustBe true

    // delete task 1
    tasksDao.deleteById(task1.id)

    val updatedtasks = tasksDao.findAll()
    updatedtasks.size mustBe 1
    updatedtasks.head.id mustBe task2.id
  }

  // validates that task and task form have the same values (with the exception of id which task form doesn't have)
  // tests will fail if the values differ
  private def taskEqualsTaskForm(task: Task, taskForm: TaskForm): Unit = {
    task.name mustBe taskForm.name
    task.description mustBe taskForm.description
    task.dueDate mustBe taskForm.dueDate
    task.completedAt mustBe taskForm.completedAt
  }

  private def rightOrFail[T](either: Either[_, T]): T = {
    either match {
      case Right(value) => value
      case Left(_) => fail("Expected either to be right, but was left")
    }
  }

}
