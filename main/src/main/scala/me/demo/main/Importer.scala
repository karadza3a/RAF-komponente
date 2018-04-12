package me.demo.main

import me.akaradzic13.schedule._
import org.joda.time.LocalDate

class Importer(var start: LocalDate,
               var end: LocalDate,
               var specialWorkdays: List[SpecialWorkday],
               var SECRETS_FILEPATH: String,
               var DATA_STORE_DIR: String
              ) {

  def createEvents(lessons: List[Lesson]): List[CalendarEvent] = {
    val sc = new ScheduleConverter(start, end, lessons, specialWorkdays)
    sc.calendarEvents
  }

  def importEvents(events: List[CalendarEvent]): Unit = {
    val G = new GoogleCalendarGenerator(SECRETS_FILEPATH, DATA_STORE_DIR)
    G.addEvents(events)
  }
}
