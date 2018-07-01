package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.RepeatConfig
import de.jotbepunkt.blackbook.persistence.SingleEvent
import de.jotbepunkt.blackbook.persistence.SingleEventRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * represents an event. Each event has an EventTypeBo which handles as a template.
 * the title, comment, tags,... are taken from the event type if not specified
 * in the event instance. Technically speaking if the property is null, the
 * corresponding value from the event type is returned otherwise the value from
 * the event itself
 */
abstract class EventBo(id: String = randomId()) : BusinessObject(id) {
    var eventType: EventTypeBo = EventTypeBo()

    var title: String? = null
        get() = if (field != null) field else eventType.title
        set(value) {
            field = value
            overrideTitle = value != null
        }
    var comment: String? = null
        get() = if (field != null) field else eventType.comment
        set(value) {
            field = value
            overrideComment = value != null
        }
    var tags: Set<TagBo>? = null
        get() = if (field != null) field else eventType.tags
        set(value) {
            field = value
            overrideTags = value != null
        }
    var publicEvent: Boolean? = null
        get() = if (field != null) field else eventType.publicEvent
        set(value) {
            field = value
            overridePublicEvent = value != null
        }
    var date = LocalDate.now()
    var fromTime = LocalTime.now()
    var length = Duration.ZERO

    @IgnoredForMapping
    var start: LocalDateTime
        get() = LocalDateTime.of(date, fromTime)
        set(value) {
            date = LocalDate.from(value)
            fromTime = LocalTime.from(value)
        }
    @IgnoredForMapping
    var end: LocalDateTime
        get() = start.plus(length)
        set(value) {
            length = Duration.between(start, value)
        }
    @IgnoredForMapping
    var overrideTitle: Boolean = false
        set(value) {
            val oldValue = field
            field = value
            if (!value && oldValue) title = null
        }
    @IgnoredForMapping
    var overrideComment: Boolean = false
        set (value) {
            val oldValue = field
            field = value
            if (!value && oldValue) comment = null
        }
    @IgnoredForMapping
    var overrideTags: Boolean = false
        set (value) {
            val oldValue = field
            field = value
            if (!value && oldValue) tags = null
        }
    @IgnoredForMapping
    var overridePublicEvent: Boolean = false
        set(value) {
            val oldValue = field
            field = value
            if (!value && oldValue) publicEvent = null
        }

}

class SingleEventBo(id: String = randomId()) : EventBo(id)

class RepeatedEventMasterBo(id: String = randomId()) : EventBo(id) {
    var repeatConfig: RepeatConfig = RepeatConfig()
}

@Service
class SingleEventService
@Autowired constructor(override val repo: SingleEventRepository, private val eventTypeService: EventTypeService)
    : BusinessService<SingleEvent, SingleEventBo>(
        createBO = { SingleEventBo() },
        createDO = { SingleEvent() }) {

    override val mappers: List<Mapper<*>>
        get() = listOf(eventTypeService)
}


