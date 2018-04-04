package me.akaradzic13.schedule

import java.time.DayOfWeek

import org.joda.time.{DateTime, LocalDate}

case class SpecialWorkday(date: LocalDate, replacementDay: Option[DayOfWeek] = Option.empty) {

}

object SpecialWorkday {

  /**
    * Alternate constructor for string arguments, useful for manual input.
    *
    * @param dateString     date to be replaced or skipped
    * @param replacementDay optional replacement day
    * @return the SpecialWorkday
    */
  def applyFromString(dateString: String, replacementDay: Option[DayOfWeek] = Option.empty): SpecialWorkday = {
    SpecialWorkday(
      DateTime.parse(dateString).toLocalDate,
      replacementDay)
  }
}