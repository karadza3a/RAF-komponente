package me.akaradzic13.schedule

import java.time.DayOfWeek

import org.joda.time.LocalTime

/**
  * Lesson as defined in the weekly schedule
  *
  * @param subject   lesson title
  * @param category  lesson type
  * @param professor professor
  * @param groups    student groups to which this lesson applies
  * @param dayOfWeek day of week
  * @param timeStart time lesson starts
  * @param timeEnd   time lesson ends
  * @param room      classroom
  */
case class Lesson(subject: String,
                  category: String,
                  professor: String,
                  groups: Set[String],
                  dayOfWeek: DayOfWeek,
                  timeStart: LocalTime,
                  timeEnd: LocalTime,
                  room: String)
