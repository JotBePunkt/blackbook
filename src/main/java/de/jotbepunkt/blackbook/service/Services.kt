package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.persistence.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.StandardPasswordEncoder
import org.springframework.stereotype.Service
import java.util.*
import javax.validation.constraints.NotBlank
import kotlin.collections.HashSet
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class IgnoredForMapping

/**
 * Base class for business services, that exposes an API with BO by wrapping the Repositories DO based API
 */
abstract class BusinessService<DO : Entity, BO : BusinessObject>(private val createDO: () -> DO, val createBO: () -> BO) : Mapper<BO> {

    class MapperImpl<out T : BusinessObject>(
            override val type: KClass<out T>,
            override val mapSingle: (String) -> T,
            override val mapMultiple: (Set<String>) -> Set<T> = { set -> HashSet(set.map(mapSingle)) }
    ) : Mapper<T>

    abstract val repo: EntityRepository<DO>
    abstract val mappers: List<Mapper<*>>

    override val type: KClass<out BO>
        get() = createBO()::class
    override val mapSingle: (String) -> BO
        get() = { it -> find(it)!! }
    override val mapMultiple: (Set<String>) -> Set<BO>
        get() = this::find

    fun findAll(): List<BO> =
            repo.findAll().map(this::toBO)


    fun find(id: String): BO? =
            repo.findById(id)
                    .map { it -> toBO(it) }
                    .orElse(null)

    fun find(ids: Set<String>): Set<BO> =
            HashSet(repo.findByIdIn(ids).map(this::toBO))


    fun save(bo: BO) =
            repo.save(toDO(bo))


    fun toDO(bo: BO): DO {
        val dataObject = createDO()

        bo::class.memberProperties.forEach { boProperty ->
            mapPropertyToDo(dataObject, boProperty, bo)
        }
        return dataObject
    }

    private fun mapPropertyToDo(dataObject: DO, boProperty: KProperty1<out BO, Any?>, bo: BO) {
        if (boProperty.isIgnored()) {
            mapUnignoredBoPropertyToDo(dataObject, boProperty, bo)
        }
    }

    private fun mapUnignoredBoPropertyToDo(dataObject: DO, boProperty: KProperty1<out BO, Any?>, bo: BO) {
        val doProperty = dataObject::class.memberProperties.find {
            it.name == boProperty.name
                    || it.name == boProperty.name + "Id"
                    || it.name == boProperty.name + "Ids"
        }

        if (doProperty != null) {
            // only mapSingle if there is a property with the same name
            if (doProperty is KMutableProperty1) {
                mapMutablePropertyToDo(boProperty, doProperty, bo, dataObject)
            } else {
                throw NotImplementedError("$doProperty is not mutable")
            }
        } else {
            throw NotImplementedError("no matching property found for $boProperty in ${dataObject::class}")
        }
    }

    private fun mapMutablePropertyToDo(boProperty: KProperty1<out BO, Any?>,
                                       doProperty: KMutableProperty1<out DO, Any?>,
                                       bo: BO, dataObject: DO) {

        if ((boProperty.returnType.classifier as KClass<*>).isSubclassOf(BusinessObject::class)
                && doProperty.returnType.classifier == String::class) {
            val value: BusinessObject = getIt(boProperty, bo)
            setIt(doProperty, dataObject, value.id)
        } else if (boProperty.returnType.isSubtypeOf(doProperty.returnType)) {
            val value: Any = getIt(boProperty, bo)
            setIt(doProperty, dataObject, value)
        } else if (isSetMapping(boProperty, doProperty)) {
            val value: Set<BusinessObject> = getIt(boProperty, bo)
            setIt(doProperty, dataObject, HashSet(value.map { it.id }))
        } else {
            throw NotImplementedError("unable to map $boProperty to $doProperty! Mapper missing?")
        }
    }

    private fun isSetMapping(boProperty: KProperty1<out BO, Any?>, doProperty: KMutableProperty1<out DO, Any?>): Boolean {
        return ((boProperty.returnType.classifier as KClass<*>).isSubclassOf(Set::class)
                && boProperty.returnType.arguments.size == 1
                && (boProperty.returnType.arguments[0].type!!.classifier as KClass<*>).isSubclassOf(BusinessObject::class)
                && (doProperty.returnType.classifier as KClass<*>).isSubclassOf(Set::class)
                && (doProperty.returnType.arguments[0].type!!.classifier == String::class))
    }


    private fun <T, X> getIt(property: KProperty1<out X, Any?>, obj: X): T =
            (property as KProperty1<X, T>).get(obj)

    private fun <T, X> setIt(doProperty1: KMutableProperty1<out X, Any?>, obj: X, value: T) {
        doProperty1.setter.isAccessible = true
        (doProperty1 as KMutableProperty1<X, T>).set(obj, value)
    }

    fun toBO(dataObject: DO): BO {
        val bo = createBO()
        val clazz = bo::class
        mapPropertiesOfClass(clazz, bo, dataObject)
        return bo
    }


    private fun mapPropertiesOfClass(clazz: KClass<out BO>, bo: BO, dataObject: DO) {
        clazz.memberProperties.forEach {
            mapPropertyToBo(it, bo, dataObject)
        }

        clazz.supertypes.filter { it.classifier != Any::class }.forEach { mapPropertiesOfClass(it.classifier as KClass<out BO>, bo, dataObject) }
    }

    private fun mapPropertyToBo(boProperty: KProperty1<out BO, Any?>, bo: BO, dataObject: DO) {
        if (boProperty.isIgnored()) {
            mapUnignoredPropertyToBo(dataObject, boProperty, bo)
        }
    }

    private fun KProperty1<out BO, Any?>.isIgnored() =
            this.annotations.none { it is IgnoredForMapping }

    private fun mapUnignoredPropertyToBo(dataObject: DO, boProperty: KProperty1<out BO, Any?>, bo: BO) {
        try {
            val doProperty = dataObject::class.memberProperties.first {
                arrayListOf("", "Id", "Ids").any { postfix ->
                    boProperty.name + postfix == it.name
                }
            }

            if (boProperty is KMutableProperty1) {
                mapMutablePropertyToBo(doProperty, boProperty, dataObject, bo)
            } else {
                throw NotImplementedError("$boProperty is not mutable")
            }
        } catch (e: NoSuchElementException) {
            throw NotImplementedError("not matching property found for $boProperty")
        }
    }

    private fun mapMutablePropertyToBo(doProperty: KProperty1<out DO, Any?>, boProperty: KMutableProperty1<out BO, Any?>, dataObject: DO, bo: BO) {
        if (boProperty.returnType.isSubtypeOf(doProperty.returnType)) {
            val value: Any = getIt(doProperty, dataObject)
            setIt(boProperty, bo, value)
        } else if (isReferenceToSingleObject(boProperty, doProperty)) {

            val mapper = findMapper(boProperty)
            val value: String = getIt(doProperty, dataObject)
            val mapped = mapper.mapSingle(value)
            setIt(boProperty, bo, mapped)

        } else if (isReferenceToSetOfBusinessObjects(boProperty, doProperty)) {

            val mapper = findMapper(boProperty)
            val value: Set<String> = getIt(doProperty, dataObject)
            val mapped = mapper.mapMultiple(value)
            setIt(boProperty, bo, mapped)

        } else {
            throw IllegalStateException("$doProperty is not mappable to $boProperty")
        }
    }

    private fun isReferenceToSetOfBusinessObjects(boProperty: KMutableProperty1<out BO, Any?>, doProperty: KProperty1<out DO, Any?>): Boolean {
        return (boProperty.returnType.classifier as KClass<*>).isSubclassOf(Set::class) &&
                (boProperty.returnType.arguments[0].type?.classifier as KClass<*>).isSubclassOf(BusinessObject::class) &&
                (doProperty.returnType.classifier as KClass<*>).isSubclassOf(Set::class) &&
                doProperty.returnType.arguments[0].type?.classifier == String::class
    }

    private fun isReferenceToSingleObject(boProperty: KMutableProperty1<out BO, Any?>, doProperty: KProperty1<out DO, Any?>) =
            (boProperty.returnType.classifier as KClass<*>).isSubclassOf(BusinessObject::class) &&
                    doProperty.returnType.classifier == String::class

    fun delete(bo: BO) {
        //TODO: Check for nested/dependent objects
        repo.deleteById(bo.id)
    }

    private fun findMapper(boProperty: KMutableProperty1<out BO, Any?>): Mapper<*> {
        try {
            return mappers.first {
                it.type == boProperty.returnType.classifier ||
                        it.type == boProperty.returnType.arguments[0].type!!.classifier
            }
        } catch (e: NoSuchElementException) {
            throw IllegalStateException("no mapper found for $boProperty")
        }
    }

    private fun <T> Optional<T>.toNullable(): T? =
            this.orElse(null)
}


interface Mapper<out T : BusinessObject> {
    val type: KClass<out T>
    val mapSingle: (String) -> T
    val mapMultiple: (Set<String>) -> Set<T>
}


abstract class BusinessObject(_id: String) {
    var id: String
        protected set

    init {
        id = _id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BusinessObject) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "BusinessObject(id='$id')"
    }

    companion object {
        fun randomId() = UUID.randomUUID().toString()
    }
}

class LanguageBusinessObject(id: String = randomId(),
                             var name: String = "",
                             var isoCode: String = "") : BusinessObject(id)

@Service
class LanguageService(@Autowired override val repo: EntityRepository<Language>)
    : BusinessService<Language, LanguageBusinessObject>({ Language() }, { LanguageBusinessObject() }) {
    override val mappers: List<Mapper<*>>
        get() = arrayListOf()
}

class TagBo(id: String = randomId(),
            var displayName: String = "",
            var tag: String = "") : BusinessObject(id) {

    override fun toString(): String = displayName
}

@Service
class TagService(@Autowired override val repo: EntityRepository<Tag>)
    : BusinessService<Tag, TagBo>({ Tag() }, { TagBo() }) {

    override val mappers: List<Mapper<*>>
        get() = arrayListOf()
}

class EventTypeBo(id: String = randomId(), var title: String = "",
                  var comment: String = "",
                  var tags: Set<TagBo> = setOf(),
                  var publicEvent: Boolean = false) : BusinessObject(id) {
    override fun toString() = title
}

@Service
class EventTypeService(@Autowired override val repo: EventTypeRepository,
                       @Autowired private val tagService: TagService) :
        BusinessService<EventType, EventTypeBo>({ EventType() }, { EventTypeBo() }) {
    override val mappers: List<Mapper<*>>
        get() = arrayListOf(tagService)

}

class UserBo(
        id: String = randomId(),
        @NotBlank var username: String = "",
        @NotBlank var name: String = "") : BusinessObject(id) {

    @NotBlank
    private var hashedPassword: String = ""

    @IgnoredForMapping
    var password: String
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

    override fun toString() = name
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

