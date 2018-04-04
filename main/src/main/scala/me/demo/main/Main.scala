package me.demo.main

import me.akaradzic13.schedule.ScheduleConverter
import org.joda.time.DateTime

object Main extends App {

  println("In Scala with...")
  println(new ScheduleConverter(DateTime.now.toLocalDate, DateTime.now.toLocalDate, List(), List()).calendarEvents)
}
