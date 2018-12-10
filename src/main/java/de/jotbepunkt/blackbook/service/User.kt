package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.User
import de.jotbepunkt.blackbook.persistence.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.StandardPasswordEncoder
import org.springframework.stereotype.Service
import javax.validation.constraints.NotBlank

class UserBo(
        id: String = randomId(),
        @NotBlank var username: String? = null,
        @NotBlank var name: String? = null) : BusinessObject(id) {

    @NotBlank
    private var hashedPassword: String = ""

    @IgnoredForMapping
    var password: String?
        get() = hashedPassword
        set(value) {
            // if the value is the same then previously, the user did not change it
            if (value != hashedPassword) {
                hashedPassword = encoder.encode(value)
            }
        }

    fun matches(password: String) =
            encoder.matches(password, hashedPassword)

    private companion object Encryption {
        val encoder = StandardPasswordEncoder("bluubs")
    }

    override fun toString() = name ?: ""
}

@Service
class UserService(@Autowired override val repo: UserRepo)
    : BusinessService<User, UserBo>({ User() }, { UserBo() }) {
    override val mappers: List<Mapper<*>>
        get() = arrayListOf()

    fun findByUsername(username: String): UserBo? =
            repo.findByUsername(username)
                    .map { it -> toBO(it) }
                    .orElse(null)
}