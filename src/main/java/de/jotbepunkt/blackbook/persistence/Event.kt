package de.jotbepunkt.blackbook.persistence

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.WeekFields

abstract class Event(id: String = randomId()) : Entity(id) {
    var eventTypeId: String? = null

    var fromTime: LocalTime? = null
    var length: Duration? = null
    var date: LocalDate? = null

    var title: String? = null
    var comment: String? = null
    var tags: Set<String>? = null
    var publicEvent: Boolean? = null
}
class SingleEvent(id: String = randomId()) : Event(id)

class RepeatedEventMaster(id: String = Entity.randomId()) : Event(id) {
    var repeatConfig = RepeatConfig()
}

class RepeatEvent(id: String = randomId(), var masterId: String) : Event(id)

class RepeatConfig {
    var weeks: Set<WeekFields> = setOf()
    var weekday: DayOfWeek = DayOfWeek.MONDAY
    var everyMonths: Int = 1
    var endDate: LocalDate = LocalDate.MAX
}

interface SingleEventRepository : EntityRepository<SingleEvent> {
    fun findByDateBetween(start: LocalDate, end: LocalDate): List<SingleEvent>
}