package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit.DAYS
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


interface EventLike {
    var title: String?
    var comment: String?
    var tags: Set<TagBo>?
    var publicEvent: Boolean?
}

/**
 * represents an event. Each event has an EventTypeBo which handles as a template.
 * the title, comment, tags,... are taken from the event type if not specified
 * in the event instance. Technically speaking if the property is null, the
 * corresponding value from the event type is returned otherwise the value from
 * the event itself
 */
abstract class EventBo<T>(id: String = randomId()) :
        BusinessObject(id), EventLike
        where T : EventLike, T : BusinessObject {

    var parent: T? = null

    inner class Overwriter<U>(var parentGetter: () -> U?) : ReadWriteProperty<EventBo<T>, U?> {

        override operator fun getValue(thisRef: EventBo<T>, property: KProperty<*>): U? {
            return _value ?: parentGetter()
        }

        override operator fun setValue(thisRef: EventBo<T>, property: KProperty<*>, value: U?) {
            _value = value
        }

        private var _value: U? = null
    }

    protected fun <U> overwrite(parentGetter: () -> U?) = Overwriter(parentGetter)

    override var title by overwrite { parent?.title }
    override var comment by overwrite { parent?.comment }
    override var tags by overwrite { parent?.tags }
    override var publicEvent by overwrite { parent?.publicEvent }

    open var date: LocalDate? = LocalDate.now()
    open var startTime: LocalTime? = LocalTime.now()
    open var length: Duration? = Duration.ZERO

    @IgnoredForMapping
    var start: LocalDateTime
        get() = LocalDateTime.of(date, startTime)
        set(value) {
            date = LocalDate.from(value)
            startTime = LocalTime.from(value)
        }

    @IgnoredForMapping
    var end: LocalDateTime
        get() = start.plus(length)
        set(value) {
            length = Duration.between(start, value)
        }

    override fun toString(): String {
        return "EventBo(parent=$parent, date=$date, " +
                "startTime=$startTime, length=$length) ${super.toString()}"
    }
}

// just a bit syntactical sugar for our test cases
fun singleEventBo(initializer: SingleEventBo.() -> Unit) =
        SingleEventBo().apply(initializer)

class SingleEventBo(id: String = randomId()) : EventBo<EventTypeBo>(id) {
    override fun toString(): String {
        return "SingleEventBo() ${super.toString()}"
    }
}

// just a bit syntactical sugar for our test cases
fun repeatedEventMasterBo(initializer: RepeatedEventMasterBo.() -> Unit) =
        RepeatedEventMasterBo().apply(initializer)

class RepeatedEventMasterBo(id: String = randomId()) : EventBo<EventTypeBo>(id), EventLike {
    var repeatConfig: RepeatConfig = RepeatConfig.None
    var endDate: LocalDate = LocalDate.MAX

    fun daily(initializer: RepeatConfig.Daily.() -> Unit) {
        repeatConfig = RepeatConfig.Daily().apply(initializer)
    }

    override fun toString(): String {
        return "repeatedEventMasterBo(repeatConfig=$repeatConfig) ${super.toString()}"
    }
}

fun repeatedEventInstanceBo(initializer: RepeatedEventInstanceBo.() -> Unit) =
        RepeatedEventInstanceBo().apply(initializer)

class RepeatedEventInstanceBo(id: String = randomId()) : EventBo<RepeatedEventMasterBo>(id) {

    override var startTime by overwrite { parent?.startTime }
    override var length by overwrite { parent?.length }

}

@Service
class SingleEventService
@Autowired constructor(override val repo: SingleEventRepository,
                       private val eventTypeService: EventTypeService,
                       private val tagService: TagService)
    : BusinessService<SingleEvent, SingleEventBo>(
        createBO = { SingleEventBo() },
        createDO = { SingleEvent() }) {


    fun findBetween(start: LocalDate, end: LocalDate): List<SingleEventBo> = repo.findByDateBetween(start, end).map { it -> toBO(it) }

    override val mappers: List<Mapper<*>>
        get() = listOf(eventTypeService, tagService)

    // TODO WRite overrides for date, startTime, duration
}

@Service
class RepeatedEventMasterService
@Autowired constructor(override val repo: RepeatedEventMasterRepository,
                       private val eventTypeService: EventTypeService,
                       private val tagService: TagService,
                       private val repeatedEventService: RepeatedEventService)
    : BusinessService<RepeatedEventMaster, RepeatedEventMasterBo>(
        createBO = { RepeatedEventMasterBo() },
        createDO = { RepeatedEventMaster() }) {

    override val mappers: List<Mapper<*>>
        get() = listOf(eventTypeService, tagService)


    override fun save(bo: RepeatedEventMasterBo): RepeatedEventMasterBo {
        assert(bo.date != null)

        val repeatConfig = bo.repeatConfig

        when (repeatConfig) {
            is RepeatConfig.None -> saveRepeatedEventInstance(bo, 0)
            is RepeatConfig.Daily -> saveDailyEvents(bo)

        }

        return super.save(bo)
    }

    private fun saveDailyEvents(bo: RepeatedEventMasterBo) {
        val repeatConfig = bo.repeatConfig as RepeatConfig.Daily

        val days = DAYS.between(bo.date, bo.endDate)
        (0..days).forEach { day ->
            if (day % repeatConfig.every == 0L) {
                saveRepeatedEventInstance(bo, day)
            }
        }
    }

    private fun saveRepeatedEventInstance(bo: RepeatedEventMasterBo, day: Long) {
        repeatedEventInstanceBo {
            parent = bo
            date = bo.date!!.plusDays(day)
        }.save()
    }

    private fun RepeatedEventInstanceBo.save() = repeatedEventService.save(this)
}


@Service
class RepeatedEventService
@Autowired constructor(override val repo: RepeatedEventRepository,
                       private val eventTypeService: EventTypeService,
                       private val tagService: TagService,
                       private val repeatedEventMasterService: RepeatedEventMasterService)
    : BusinessService<RepeatedEvent, RepeatedEventInstanceBo>(
        createBO = { RepeatedEventInstanceBo() },
        createDO = { RepeatedEvent() }) {

    override val mappers: List<Mapper<*>>
        get() = listOf(eventTypeService, tagService, repeatedEventMasterService)
}