package db

import models.TaskForm
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.TestUtil

class TasksDaoSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfter with TestUtil {

  // before each test runs delete everything from the database
  before {
    cleanupDb()
  }

  "insert into tasks with empty completed at" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None)
    val taskEither = tasksDao.insert(taskForm)
    val task = rightOrError(taskEither)
    taskEqualsTaskForm(task, taskForm) mustBe true
  }

  "insert into tasks when completed at is not empty" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), Some(new LocalDate(2018, 8, 20)))
    val taskEither = tasksDao.insert(taskForm)
    val task = rightOrError(taskEither)
    taskEqualsTaskForm(task, taskForm) mustBe true
  }

  "update task" in {
    val dueDate = new LocalDate(2018, 8, 18)
    val completedAt = new LocalDate(2018, 8, 20)
    val taskForm1 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm3 = TaskForm(randomString, "My first task", dueDate, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))

    taskEqualsTaskForm(task1, taskForm1) mustBe true
    taskEqualsTaskForm(task2, taskForm2) mustBe true
    taskEqualsTaskForm(task3, taskForm3) mustBe true

    val updatedForm = taskForm1.copy(completedAt = Some(completedAt))
    val updatedTask = tasksDao.updateById(task1.id, updatedForm).getOrElse(fail(s"Error updating task with id: ${task1.id}"))

    // only task1 should have changed
    taskEqualsTaskForm(updatedTask, updatedForm) mustBe true
    taskEqualsTaskForm(updatedTask, taskForm1) mustBe false
    taskEqualsTaskForm(task2, taskForm2) mustBe true
    taskEqualsTaskForm(task3, taskForm3) mustBe true
  }

  "find tasks by id" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None)
    val taskEither = tasksDao.insert(taskForm)
    val task = rightOrError(taskEither)

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

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))

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

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))

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

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))

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

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))

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

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))

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

}
