package me.akaradzic13.schedule

import java.time.DayOfWeek

import com.google.api.services.calendar.model.CalendarListEntry
import com.univocity.parsers.csv.{CsvParser, CsvParserSettings}
import org.joda.time.{DateTime, LocalTime}

import scala.collection.JavaConverters._
import scala.util.Random

object Main extends App {

  def parserTest = {
    val start = DateTime.parse("2018-01-01").toLocalDate
    val end = start.plusDays(10)
    val parser = new CsvParser(new CsvParserSettings())

    import java.io.{FileInputStream, InputStreamReader}

    val csv: List[Array[String]] = parser.parseAll(
      new InputStreamReader(
        new FileInputStream("/Users/andrejk/Downloads/raspored.csv"), "UTF-8")
    ).asScala.toList

    def parseDayOfWeek(str: String): DayOfWeek = {
      str match {
        case "PON" => DayOfWeek.MONDAY
        case "UTO" => DayOfWeek.TUESDAY
        case "SRE" => DayOfWeek.WEDNESDAY
        case "ČET" => DayOfWeek.THURSDAY
        case "PET" => DayOfWeek.FRIDAY
        case "SUB" => DayOfWeek.SATURDAY
        case "NED" => DayOfWeek.SUNDAY
      }
    }

    // replace unbreakable spaces (U+0160) and trim
    def trimmed(str: String): String = str.replace('\u00A0', ' ').trim

    val lessons: List[Lesson] = csv.slice(1, csv.size).map { row =>
      Lesson(
        row(0),
        row(1),
        row(2),
        row(3).split(",").map(trimmed).toSet,
        parseDayOfWeek(trimmed(row(4))),
        LocalTime.parse(row(5).split("-")(0)),
        LocalTime.parse(row(5).split("-")(1)),
        trimmed(row(6))
      )
    }

    val lessonsSubset = Random.shuffle(lessons).take(12)
    println(lessonsSubset)

    val specialWorkdays = List(
      SpecialWorkday.applyFromString("2018-01-05"),
      SpecialWorkday.applyFromString("2018-01-06"),
      SpecialWorkday.applyFromString("2018-01-07"),
      SpecialWorkday.applyFromString("2018-01-08"),
      SpecialWorkday.applyFromString("2018-01-10", Some(DayOfWeek.FRIDAY))
    )

    println(new ScheduleConverter(start, end, lessonsSubset, specialWorkdays).calendarEvents)
  }

  def googleTest = {
    val G = new GoogleCalendarGenerator()

    G.calendar

    val calendarList = G.client.calendarList.list.execute

    if (calendarList.getItems != null) {
      for (entry: CalendarListEntry <- calendarList.getItems.asScala) {

        println("ID: " + entry.getId)
        println("Summary: " + entry.getSummary)
        if (entry.getDescription != null)
          println("Description: " + entry.getDescription)
      }
      println("-----------------------------------------------")
    }

  }

  //  parserTest
  googleTest
}
