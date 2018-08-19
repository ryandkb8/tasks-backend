package utils

import org.joda.time.LocalDate
import play.api.libs.json._

object LocalDateUtil {

  private val LocalDatetimePattern = "yyyy-MM-dd"

  implicit val localDateTimeReads: Reads[LocalDate] = JodaReads.jodaLocalDateReads(LocalDatetimePattern)
  implicit val localDateTimeWrites: Writes[LocalDate] = JodaWrites.jodaLocalDateWrites(LocalDatetimePattern)

}
