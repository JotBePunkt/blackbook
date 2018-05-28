package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.EntityRepository
import de.jotbepunkt.blackbook.persistence.Tag
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TagService(@Autowired override val repo: EntityRepository<Tag>)
    : BusinessService<Tag, TagBo>({ Tag() }, { TagBo() }) {

    override val mappers: List<Mapper<*>>
        get() = arrayListOf()
}

class TagBo(id: String = randomId(),
            var displayName: String = "",
            var tag: String = "") : BusinessObject(id) {

    override fun toString(): String = displayName
}