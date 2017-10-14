package de.jotbepunkt.blackbook.persistence

/**
 * Created by bait on 30.09.17.
 */

class User(id: String = randomId()) : Entity(id) {
    var username: String = ""
    var name: String = ""
    var hashedPassword: String = ""
}

interface UserRepo : EntityRepository<User>