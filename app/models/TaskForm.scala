package models

import org.joda.time.LocalDate
import play.api.libs.functional.syntax._
import play.api.libs.json._
import utils.LocalDateUtil._

case class TaskForm(
  name: String,
  description: String,
  dueDate: LocalDate,
  completedAt: Option[LocalDate]
)

object TaskForm {

  implicit val TaskFormWrites: Writes[TaskForm] = new Writes[TaskForm] {
    def writes(form: TaskForm): JsObject = Json.obj(
      "name" -> form.name,
      "description" -> form.description,
      "due_date" -> form.dueDate.toString(),
      "completed_at" -> form.completedAt.map(_.toString())
    )
  }

  implicit val TaskFormReads: Reads[TaskForm] = (
      (JsPath \ "name").read[String] and
      (JsPath \ "description").read[String] and
      (JsPath \ "due_date").read[LocalDate] and
      (JsPath \ "completed_at").readNullable[LocalDate]
    )(TaskForm.apply _)

}