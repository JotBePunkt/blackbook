package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.EventType
import de.jotbepunkt.blackbook.persistence.EventTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

fun eventTypeBo(initializer: EventTypeBo.() -> Unit) =
        EventTypeBo().apply(initializer)

class EventTypeBo(id: String = randomId(),
                  override var title: String? = "",
                  override var comment: String? = "",
                  override var tags: Set<TagBo>? = setOf(),
                  override var publicEvent: Boolean? = false) : BusinessObject(id), EventLike {
    override fun toString() = title ?: ""
}

@Service
class EventTypeService(@Autowired override val repo: EventTypeRepository,
                       @Autowired private val tagService: TagService) :
        BusinessService<EventType, EventTypeBo>({ EventType() }, { EventTypeBo() }) {
    override val mappers: List<Mapper<*>>
        get() = arrayListOf(tagService)

}