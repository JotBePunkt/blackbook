package de.jotbepunkt.blackbook.persistence

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.util.*

abstract class Event(id: String = randomId()) : Entity(id) {
    var parentId: String? = null

    var startTime: LocalTime? = null
    var length: Duration? = null
    var date: LocalDate? = null

    var title: String? = null
    var comment: String? = null
    var tags: Set<String>? = null
    var publicEvent: Boolean? = null
}

/**
 * represents a single non repeated event
 */
class SingleEvent(id: String = randomId()) : Event(id)

/**
 * represents a repeated event with its regular repeating. The date is the first
 * date when the event occurs. The master is typically not shown in the calendar
 * as it represents the idealtypical occuring event. Each occurence is
 * represented in a RepeatedEvent.
 */
class RepeatedEventMaster(id: String = Entity.randomId()) : Event(id) {

    var endDate: LocalDate = LocalDate.MAX
    var repeatConfig: RepeatConfig? = null

    fun daily(initializer: RepeatConfig.Daily.() -> Unit) {
        repeatConfig = RepeatConfig.Daily().apply(initializer)
    }
}

sealed class RepeatConfig {
    object None : RepeatConfig()

    class Daily : RepeatConfig() {
        var every: Int = 1
            set(value) {
                assert(value > 0)
                field = value
            }
    }
}

/**
 * represents am occurence of a single event. In most cases it is generated
 * using the repeatconfig of the master but its date/start/ende date can also be
 * altered
 */
class RepeatedEvent(id: String = randomId()) : Event(id) {

    var masterId: String? = null
        set(value) {
            if (value == null)
                throw IllegalArgumentException("Null is not allowed to set")
            else {
                // validate that its a uuid
                field = UUID.fromString(value).toString()
            }
        }
}

interface SingleEventRepository : EntityRepository<SingleEvent> {
    fun findByDateBetween(start: LocalDate, end: LocalDate): List<SingleEvent>
}

interface RepeatedEventMasterRepository : EntityRepository<RepeatedEventMaster>

interface RepeatedEventRepository : EntityRepository<RepeatedEvent> {
    fun findByDateBetween(start: LocalDate, end: LocalDate): List<SingleEvent>
    fun findByMasterId(masterId: String)
}