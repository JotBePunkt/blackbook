package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.Tag
import de.jotbepunkt.blackbook.persistence.TagRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TagService(@Autowired override val repo: TagRepository)
    : BusinessService<Tag, TagBo>({ Tag() }, { TagBo() }) {

    override val mappers: List<Mapper<*>>
        get() = arrayListOf()
}

fun tagBo(initializer: TagBo.() -> Unit) =
        TagBo().apply(initializer)

class TagBo(id: String = randomId(),
            var displayName: String? = null,
            var tag: String? = null) : BusinessObject(id) {

    override fun toString(): String = displayName ?: ""
}