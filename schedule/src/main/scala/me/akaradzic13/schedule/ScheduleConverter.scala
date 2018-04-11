package me.akaradzic13.schedule

import java.time.DayOfWeek

import org.joda.time.{Days, LocalDate}

/**
  * Converts list of [[Lesson]]s to list of [[CalendarEvent]]s with additional options specified below.
  *
  * @param start           schedule effective start time
  * @param end             schedule effective end time
  * @param lessons         list of lessons which should be converted to events
  * @param specialWorkdays non-working days and days with replacements
  */
class ScheduleConverter(start: LocalDate,
                        end: LocalDate,
                        lessons: List[Lesson],
                        specialWorkdays: List[SpecialWorkday]) {

  private val specialWorkdaysMap: Map[LocalDate, SpecialWorkday] = specialWorkdays.map(sw => sw.date -> sw).toMap
  private val lessonsMap: Map[DayOfWeek, List[Lesson]] = lessons.groupBy(_.dayOfWeek)

  /**
    * Gets day of week for specified date taking into account special workdays.
    *
    * @param date date
    * @return optional day of week for the specified date or none if the day is non-working
    */
  def getEffectiveDoW(date: LocalDate): Option[DayOfWeek] = {
    specialWorkdaysMap.get(date) match {
      case None => Some(DayOfWeek.of(date.getDayOfWeek))
      case Some(specialWorkday) => specialWorkday.replacementDay
    }
  }

  /**
    * Generates a list of calendar events for given lessons and options.
    *
    * @return list of events
    */
  def calendarEvents: List[CalendarEvent] = {
    val daysCount = Days.daysBetween(start, end).getDays
    val dates = (0 until daysCount).map(start.plusDays).toList

    // flatMap removes 'None' Options, flatten converts List[List]] to a single List
    dates.flatMap { date =>
      getEffectiveDoW(date).map { dow =>
        val lessonsForDay: List[Lesson] = lessonsMap.getOrElse(dow, List())

        lessonsForDay.map { lesson =>
          CalendarEvent(lesson, date)
        }
      }.toList
    }.flatten
  }

}

