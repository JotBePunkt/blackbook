package de.jotbepunkt.blackbook.persistence

import de.jotbepunkt.blackbook.masterdata.Entity
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*
import javax.validation.constraints.Size


interface TagRepository : MongoRepository<Tag, String> {

}


class Tag(@Size(min = 1) var displayName: String = "",
          @Size(min = 1) var tag: String = "") : Entity() {

    override fun toString(): String {
        return displayName
    }
}