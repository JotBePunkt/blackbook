package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.EventType
import de.jotbepunkt.blackbook.persistence.EventTypeRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

class EventTypeBo(id: String = randomId(), var title: String = "",
                  var comment: String = "",
                  var tags: Set<TagBo> = setOf(),
                  var publicEvent: Boolean = false) : BusinessObject(id) {
    override fun toString() = title
}

@Service
class EventTypeService(@Autowired override val repo: EventTypeRepository,
                       @Autowired private val tagService: TagService) :
        BusinessService<EventType, EventTypeBo>({ EventType() }, { EventTypeBo() }) {
    override val mappers: List<Mapper<*>>
        get() = arrayListOf(tagService)

}