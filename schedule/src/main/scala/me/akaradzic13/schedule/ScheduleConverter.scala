package me.akaradzic13.schedule

import java.time.DayOfWeek

import org.joda.time.{Days, LocalDate}

class ScheduleConverter(start: LocalDate,
                        end: LocalDate,
                        lessons: List[Lesson],
                        specialWorkdays: List[SpecialWorkday]) {

  val specialWorkdaysMap: Map[LocalDate, SpecialWorkday] = specialWorkdays.map(sw => sw.date -> sw).toMap
  val lessonsMap: Map[DayOfWeek, List[Lesson]] = lessons.groupBy(_.dayOfWeek)

  def getEffectiveDoW(date: LocalDate): Option[DayOfWeek] = {
    specialWorkdaysMap.get(date) match {
      case None => Some(DayOfWeek.of(date.getDayOfWeek))
      case Some(specialWorkday) => specialWorkday.replacementDay
    }
  }

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

