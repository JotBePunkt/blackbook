package de.jotbepunkt.blackbook.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

interface EntityRepository<DO : Entity> : MongoRepository<DO, String> {
    fun findByIdIn(ids: Set<String>): List<DO>

}

abstract class Entity(@Id var id: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Entity) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Entity(id='$id')"
    }

    companion object {
        fun randomId(): String = UUID.randomUUID().toString()
    }
}