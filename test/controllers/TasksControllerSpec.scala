package controllers

import db.Task
import db.TasksDao.TaskReads
import models.TaskForm
import models.TaskForm._
import org.joda.time.LocalDate
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.libs.json.Json
import play.mvc.Http
import utils.TestUtil

class TasksControllerSpec extends PlaySpec with GuiceOneServerPerSuite with BeforeAndAfter with TestUtil {

  // before each test runs delete everything from the database
  before {
    cleanupDb()
  }

  private val BaseUrl = s"http://localhost:$port/"
  private def taskClient = wsClient.url(BaseUrl + "task")

  "GET /task returns all tasks" in  {
    val response = await(taskClient.get())
    response.status mustBe Http.Status.OK

    // there should be no tasks
    val tasks = Json.parse(response.body).as[Seq[Task]]
    tasks.size mustBe 0

    // add 3 tasks
    val dueDate = new LocalDate(2018, 8, 18)
    val completedAt = new LocalDate(2018, 8, 17)

    val taskForm1 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm3 = TaskForm(randomString, "My first task", dueDate, Some(completedAt))

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))

    val response2 = await(taskClient.get())
    response2.status mustBe Http.Status.OK

    val tasks2 = Json.parse(response2.body).as[Seq[Task]]
    tasks2.size mustBe 3
    tasks2.contains(task1) mustBe true
    tasks2.contains(task2) mustBe true
    tasks2.contains(task3) mustBe true
  }

  "GET /tasks/:id returns task" in {
    // add 3 tasks
    val dueDate = new LocalDate(2018, 8, 18)
    val completedAt = new LocalDate(2018, 8, 17)

    val taskForm1 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm3 = TaskForm(randomString, "My first task", dueDate, Some(completedAt))

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))

    val client = wsClient.url(BaseUrl + "task/" + task2.id)
    val response = await(client.get())
    response.status mustBe Http.Status.OK
    val taskFound = Json.parse(response.body).as[Task]
    taskFound mustBe task2
  }

  "GET /tasks/:id returns 404 when task doesn't exist" in {
    val client = wsClient.url(BaseUrl + "task/" + Long.MaxValue.toString)
    val response = await(client.get())
    response.status mustBe Http.Status.NOT_FOUND
  }

  "GET /tasks due today" in {
    // add 5 tasks
    val today = new LocalDate(2018, 8, 18)
    val tomorrow = today.plusDays(1)
    val dayAfterTomorrow = today.plusDays(2)
    val completedAt = new LocalDate(2018, 8, 17)
    clock.setDate(today)

    val taskForm1 = TaskForm(randomString, "My first task", today, None)
    val taskForm2 = TaskForm(randomString, "My first task", today, None)
    val taskForm3 = TaskForm(randomString, "My first task", tomorrow, Some(completedAt))
    val taskForm4 = TaskForm(randomString, "My first task", tomorrow, None)
    val taskForm5 = TaskForm(randomString, "My first task", dayAfterTomorrow, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))
    val task5 = rightOrError(tasksDao.insert(taskForm5))

    val response = await(taskClient.withQueryStringParameters("dueToday" -> "true", "dueTomorrow" -> "false").get())
    response.status mustBe Http.Status.OK
    val tasks = Json.parse(response.body).as[Seq[Task]]
    tasks.size mustBe 2
    tasks.contains(task1) mustBe true
    tasks.contains(task2) mustBe true
  }

  "GET /tasks due tomorrow" in {
    // add 5 tasks
    val today = new LocalDate(2018, 8, 18)
    val tomorrow = today.plusDays(1)
    val dayAfterTomorrow = today.plusDays(2)
    val completedAt = new LocalDate(2018, 8, 17)
    clock.setDate(today)

    val taskForm1 = TaskForm(randomString, "My first task", today, None)
    val taskForm2 = TaskForm(randomString, "My first task", today, None)
    val taskForm3 = TaskForm(randomString, "My first task", tomorrow, Some(completedAt))
    val taskForm4 = TaskForm(randomString, "My first task", tomorrow, None)
    val taskForm5 = TaskForm(randomString, "My first task", dayAfterTomorrow, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))
    val task5 = rightOrError(tasksDao.insert(taskForm5))

    val response = await(taskClient.withQueryStringParameters("dueToday" -> "false", "dueTomorrow" -> "true").get())
    response.status mustBe Http.Status.OK
    val tasks = Json.parse(response.body).as[Seq[Task]]
    tasks.size mustBe 1
    tasks.contains(task4) mustBe true
  }

  "GET /tasks due today and tomorrow" in {
    // add 5 tasks
    val today = new LocalDate(2018, 8, 18)
    val tomorrow = today.plusDays(1)
    val dayAfterTomorrow = today.plusDays(2)
    val completedAt = new LocalDate(2018, 8, 17)
    clock.setDate(today)

    val taskForm1 = TaskForm(randomString, "My first task", today, None)
    val taskForm2 = TaskForm(randomString, "My first task", today, None)
    val taskForm3 = TaskForm(randomString, "My first task", tomorrow, Some(completedAt))
    val taskForm4 = TaskForm(randomString, "My first task", tomorrow, None)
    val taskForm5 = TaskForm(randomString, "My first task", dayAfterTomorrow, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))
    val task5 = rightOrError(tasksDao.insert(taskForm5))

    val response = await(taskClient.withQueryStringParameters("dueToday" -> "true", "dueTomorrow" -> "true").get())
    response.status mustBe Http.Status.OK
    val tasks = Json.parse(response.body).as[Seq[Task]]
    tasks.size mustBe 3
    tasks.contains(task1) mustBe true
    tasks.contains(task2) mustBe true
    tasks.contains(task4) mustBe true
  }

  "GET /tasks finds overdue tasks" in {
    // add 5 tasks
    val today = new LocalDate(2018, 8, 18)
    val dayBeforeToday = today.minusDays(1)
    val completedAt = new LocalDate(2018, 8, 17)
    clock.setDate(today)

    val taskForm1 = TaskForm(randomString, "My first task", dayBeforeToday, None)
    val taskForm2 = TaskForm(randomString, "My first task", dayBeforeToday, None)
    val taskForm3 = TaskForm(randomString, "My first task", dayBeforeToday, Some(completedAt))
    val taskForm4 = TaskForm(randomString, "My first task", today, None)
    val taskForm5 = TaskForm(randomString, "My first task", today, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))
    val task5 = rightOrError(tasksDao.insert(taskForm5))

    val response = await(taskClient.withQueryStringParameters("overdue" -> "true").get())
    response.status mustBe Http.Status.OK
    val tasks = Json.parse(response.body).as[Seq[Task]]
    tasks.size mustBe 2
    tasks.contains(task1) mustBe true
    tasks.contains(task2) mustBe true
  }

  "GET /tasks finds completed tasks" in {
    // add 5 tasks
    val dueDate = new LocalDate(2018, 8, 18)
    val completedAt = new LocalDate(2018, 8, 17)

    val taskForm1 = TaskForm(randomString, "My first task", dueDate, Some(completedAt))
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm3 = TaskForm(randomString, "My first task", dueDate, Some(completedAt))
    val taskForm4 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm5 = TaskForm(randomString, "My first task", dueDate, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    val task3 = rightOrError(tasksDao.insert(taskForm3))
    val task4 = rightOrError(tasksDao.insert(taskForm4))
    val task5 = rightOrError(tasksDao.insert(taskForm5))

    val response = await(taskClient.withQueryStringParameters("completed" -> "true").get())
    response.status mustBe Http.Status.OK
    val tasks = Json.parse(response.body).as[Seq[Task]]
    tasks.size mustBe 2
    tasks.contains(task1) mustBe true
    tasks.contains(task3) mustBe true
  }

  "POST /task creates a task" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None)
    val response = await(taskClient.post(Json.toJson(taskForm)))
    response.status mustBe Http.Status.CREATED

    val taskCreated = Json.parse(response.body).as[Task]
    taskEqualsTaskForm(taskCreated, taskForm) mustBe true

    val taskFound = tasksDao.findById(taskCreated.id)
    taskFound.contains(taskCreated) mustBe true
  }

  "POST /task returns a 422 when body is invalid" in {
    val response = await(taskClient.post("invalid body"))
    response.status mustBe Http.Status.UNPROCESSABLE_ENTITY
  }

  "PUT /task/id updates a task" in {
    // add 2 tasks
    val dueDate = new LocalDate(2018, 8, 18)
    val completedAt = new LocalDate(2018, 8, 17)

    val taskForm1 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))

    val client = wsClient.url(BaseUrl + "task/" + task1.id)
    val updatedForm = taskForm1.copy(description = "My updated task", completedAt = Some(completedAt))
    val response = await(client.put(Json.toJson(updatedForm)))
    response.status mustBe Http.Status.OK

    val updatedTask = Json.parse(response.body).as[Task]
    taskEqualsTaskForm(updatedTask, updatedForm) mustBe true
  }

  "PUT /task/:id returns a 422 when body is invalid" in {
    val taskForm = TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None)
    val task = rightOrError(tasksDao.insert(taskForm))

    val client = wsClient.url(BaseUrl + "task/" + task.id)
    val response = await(client.put("invalid body"))
    response.status mustBe Http.Status.UNPROCESSABLE_ENTITY
  }

  "PUT /task/:id returns 404 when task doesn't exist" in {
    val client = wsClient.url(BaseUrl + "task/" + Long.MaxValue)
    val response = await(client.put(Json.toJson(TaskForm(randomString, "My first task", new LocalDate(2018, 8, 18), None))))
    response.status mustBe Http.Status.NOT_FOUND
  }

  "DELETE /task/:id deletes a task" in {
    // add 2 tasks
    val dueDate = new LocalDate(2018, 8, 18)
    val completedAt = new LocalDate(2018, 8, 17)

    val taskForm1 = TaskForm(randomString, "My first task", dueDate, None)
    val taskForm2 = TaskForm(randomString, "My first task", dueDate, None)

    val task1 = rightOrError(tasksDao.insert(taskForm1))
    val task2 = rightOrError(tasksDao.insert(taskForm2))
    tasksDao.findAll().size mustBe 2

    val client = wsClient.url(BaseUrl + "task/" + task1.id)
    val response = await(client.delete())
    response.status mustBe Http.Status.OK

    val tasks = tasksDao.findAll()
    tasks.size mustBe 1
    tasks.contains(task2) mustBe true
  }

  "DELETE /task/:id returns a 404 when a task isn't found" in {
    val client = wsClient.url(BaseUrl + "task/" + Long.MaxValue)
    val response = await(client.delete())
    response.status mustBe Http.Status.NOT_FOUND
  }


}
