package me.akaradzic13.schedule

import java.time.DayOfWeek

import org.joda.time.LocalTime

case class Lesson(subject: String,
                  category: String,
                  professor: String,
                  groups: Set[String],
                  dayOfWeek: DayOfWeek,
                  timeStart: LocalTime,
                  timeEnd: LocalTime,
                  room: String)
