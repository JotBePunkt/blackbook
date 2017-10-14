package de.jotbepunkt.blackbook.persistence

/**
 * Created by bait on 18.07.17.
 */
class Language(id: String = randomId()) : Entity(id) {
    var isoCode = ""
    var name = ""

    override fun toString(): String {
        return name
    }
}

interface LanguageRepository : EntityRepository<Language>