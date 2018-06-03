package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.RepeatConfig
import de.jotbepunkt.blackbook.persistence.SingleEvent
import de.jotbepunkt.blackbook.persistence.SingleEventRepository
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime

abstract class EventBo(id: String = randomId()) : BusinessObject(id) {
    var eventType: EventTypeBo = EventTypeBo()

    var comment: String? = null
        get() = if (field != null) field else eventType.comment
    var tags: Set<TagBo>? = null
        get() = if (field != null) field else eventType.tags
    var publicEvent: Boolean? = null
        get() = if (field != null) field else eventType.publicEvent

    var startDate = LocalDate.now()
    var startTime = LocalTime.now()
    var length = Duration.ZERO
}

class SingleEventBo(id: String = randomId()) : EventBo(id)

class RepeatedEventMasterBo(id: String = randomId()) : EventBo(id) {
    var repeatConfig: RepeatConfig = RepeatConfig()
}

class SingleEventService
@Autowired constructor(override val repo: SingleEventRepository, private val eventTypeService: EventTypeService)
    : BusinessService<SingleEvent, SingleEventBo>(
        createBO = { SingleEventBo() },
        createDO = { SingleEvent() }) {

    override val mappers: List<Mapper<*>>
        get() = listOf(eventTypeService)
}


