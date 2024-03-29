package me.akaradzic13.schedule

import org.joda.time.{DateTime, DateTimeZone, LocalDate}

/**
  * Event model used by [[GoogleCalendarGenerator]] to populate user's calendar.
  *
  * @param title       event summary
  * @param description event description
  * @param location    event location
  * @param start       event start [DateTime]
  * @param end         event end [DateTime]
  */
case class CalendarEvent(title: String,
                         description: String,
                         location: String,
                         start: DateTime,
                         end: DateTime) {}

object CalendarEvent {
  def apply(lesson: Lesson, date: LocalDate, timeZone: DateTimeZone = DateTimeZone.forID("Europe/Belgrade")): CalendarEvent = {
    val cat: String = lesson.category match {
      case "Predavanja" => "P"
      case "Vezbe" => "V"
      case "Predavanja i vezbe" => "P+V"
      case "Laboratorijske vezbe" => "Lab"
      case _ => lesson.category
    }
    val groups = lesson.groups.mkString(",")
    new CalendarEvent(
      s"${lesson.subject} [$cat]",
      s"Predavač: ${lesson.professor}, grupe: $groups",
      lesson.room,
      date.toDateTime(lesson.timeStart).withZone(timeZone),
      date.toDateTime(lesson.timeEnd).withZone(timeZone)
    )
  }
}
