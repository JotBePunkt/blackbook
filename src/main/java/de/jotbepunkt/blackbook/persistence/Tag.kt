package de.jotbepunkt.blackbook.persistence

import javax.validation.constraints.Size


interface TagRepository : EntityRepository<Tag> {

}


class Tag(id: String = randomId(),
          @Size(min = 1) var displayName: String = "",
          @Size(min = 1) var tag: String = "") : Entity(id) {

    override fun toString(): String {
        return displayName
    }
}