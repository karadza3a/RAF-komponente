package me.demo.main

import java.time.DayOfWeek

import me.akaradzic13.schedule._
import org.joda.time.DateTime

object Main extends App {

  def parserTest: List[Lesson] = {
    val path = "/Users/andrejk/Downloads/raspored.csv"

    val parser = new Parser(path)
    val lessons = parser.parseLessons

    lessons
  }

  def importerTest(lessons: List[Lesson]): List[CalendarEvent] = {
    val start = DateTime.parse("2018-04-03").toLocalDate
    val end = start.plusDays(10)
    val specialWorkdays = List(
      SpecialWorkday.applyFromString("2018-04-06"),
      SpecialWorkday.applyFromString("2018-04-07"),
      SpecialWorkday.applyFromString("2018-04-08"),
      SpecialWorkday.applyFromString("2018-04-09"),
      SpecialWorkday.applyFromString("2018-04-10", Some(DayOfWeek.FRIDAY))
    )

    val importer = new Importer(start, end, specialWorkdays,
      "/Users/andrejk/Downloads/client_secrets.json",
      "/Users/andrejk/IdeaProjects/komponente/schedule/store"
    )

    val events = importer.createEvents(lessons)
    importer.importEvents(events)

    events
  }

  val lessons = parserTest
  val lessonsSubset = lessons.filter { lesson =>
    val subject = lesson.subject.toLowerCase
    subject.contains("napredna matema") ||
      subject.contains("softverske komp") ||
      subject.contains("kompresija poda") ||
      (subject.contains("teorija algoritama") && lesson.dayOfWeek == DayOfWeek.WEDNESDAY)
  }
  println(lessonsSubset)

  val events = importerTest(lessonsSubset)
  println(events)
}
