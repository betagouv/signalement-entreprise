package utils

import controllers.error.ApiError.MalformedValue

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateUtils {

  val DATE_FORMAT = "yyyy-MM-dd"
  val FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT)
  val TIME_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  def parseDate(source: Option[String]): Option[LocalDate] =
    try source.map(s => LocalDate.parse(s, FORMATTER))
    catch {
      case _: DateTimeParseException => None
    }

  def parseTime(source: Option[String]): Option[OffsetDateTime] =
    try source.map(s => OffsetDateTime.parse(s, TIME_FORMATTER))
    catch {
      case _: DateTimeParseException => None
    }

  def parseTime(source: String): OffsetDateTime =
    try OffsetDateTime.parse(source, TIME_FORMATTER)
    catch {
      case _: DateTimeParseException => throw MalformedValue(source, "OffsetDateTime")
    }

}
