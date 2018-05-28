package de.jotbepunkt.blackbook.persistence

import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.WeekFields

interface EventRepository : EntityRepository<Event>

abstract class Event(id: String = randomId()) : Entity(id) {
    var eventTypeId: String? = null

    var fromTime: LocalTime? = null
    var length: Duration? = null
    var date = null

    var title: String? = null
    var comment: String? = null
    var tags: Set<String>? = null
    var publicEvent: Boolean? = null
}

@Document(collection = "event")
@TypeAlias("single")
class SingleEvent(id: String = randomId()) : Event(id)

@Document(collection = "event")
@TypeAlias("repeated-master")
class RepeatedEventMaster(id: String = Entity.randomId()) : Event(id) {
    var repeatConfig = RepeatConfig()
}

@Document(collection = "event")
@TypeAlias("repeated")
class RepeatEvent(id: String = randomId(), var masterId: String) : Event(id)

class RepeatConfig {
    var weeks: Set<WeekFields> = setOf()
    var weekday: DayOfWeek = DayOfWeek.MONDAY
    var everyMonths: Int = 1
    var endDate: LocalDate = LocalDate.MAX
}
