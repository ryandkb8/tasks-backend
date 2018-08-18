package models

import org.joda.time.LocalDate

case class TaskForm(
  name: String,
  description: String,
  dueDate: LocalDate,
  completedAt: Option[LocalDate]
)
