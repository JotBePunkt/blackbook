package de.jotbepunkt.blackbook.persistence

/**
 * Created by bait on 07.07.17.
 */

interface EventTypeRepository : EntityRepository<EventType>

class EventType(id: String = randomId()) : Entity(id) {

    var title: String = ""
    var comment: String = ""
    var tags: List<String> = ArrayList()
    var publicEvent: Boolean = true

}