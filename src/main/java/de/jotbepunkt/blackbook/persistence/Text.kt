package de.jotbepunkt.blackbook.persistence

import java.time.Instant

interface TextRepository : EntityRepository<Text>

class Text(id: String = randomId()) : Entity(id) {
    val translations: Map<String, Translation> = mapOf()
    var comments: String = ""
}

class Translation(id: String = randomId()) : Entity(id) {
    var text = ""
    var lastChange = Instant.now()
}