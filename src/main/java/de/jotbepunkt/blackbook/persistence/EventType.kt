package de.jotbepunkt.blackbook.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*
import kotlin.collections.HashSet

/**
 * Created by bait on 07.07.17.
 */

interface EventTypeRepository : MongoRepository<EventType, String>

class EventType {

    @Id var id: String = UUID.randomUUID().toString()
    var title: String = ""
    var comment: String = ""
    var tags: Set<Tag> = HashSet()
    var publicEvent: Boolean = true

}