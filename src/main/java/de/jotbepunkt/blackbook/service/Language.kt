package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.EntityRepository
import de.jotbepunkt.blackbook.persistence.Language
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

class LanguageBusinessObject(id: String = randomId(),
                             var name: String? = null,
                             var isoCode: String? = null) : BusinessObject(id)

@Service
class LanguageService(@Autowired override val repo: EntityRepository<Language>)
    : BusinessService<Language, LanguageBusinessObject>({ Language() }, { LanguageBusinessObject() }) {
    override val mappers: List<Mapper<*>>
        get() = arrayListOf()
}