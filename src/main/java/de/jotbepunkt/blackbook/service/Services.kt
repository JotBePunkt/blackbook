package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.masterdata.Entity
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.memberProperties

/**
 * Created by bait on 18.07.17.
 */
abstract class Service<DO : Entity, BO : BusinessObject>(val createDO: () -> DO, val createBO: () -> BO) {

    class Mapper<T : BusinessObject>(val type: KClass<T>, val map: (String) -> T)


    abstract val repo: MongoRepository<DO, String>
    abstract val mappers: List<Mapper<*>>

    fun findAll() {
        repo.findAll().map(this::toBO)
    }

    fun save(bo: BO) {
        repo.save(toDO(bo))
    }

    fun toDO(bo: BO): DO {
        val dataObject = createDO()

        bo::class.memberProperties.forEach { boProperty ->
            mapPropertyToDo(dataObject, boProperty, bo)
        }
        return dataObject
    }

    private fun mapPropertyToDo(dataObject: DO, boProperty: KProperty1<out BO, Any?>, bo: BO) {
        val doProperty = dataObject::class.memberProperties.find {
            it.name == boProperty.name || it.name == boProperty.name + "Id"
        }

        if (doProperty != null) {
            // only map if there is a property with the same name
            if (doProperty is KMutableProperty1) {
                mapMutablePropertyToDo(boProperty, doProperty, bo, dataObject)
            } else {
                throw IllegalStateException("$doProperty is not mutable")
            }
        }
    }

    private fun mapMutablePropertyToDo(boProperty: KProperty1<out BO, Any?>,
                                       doProperty: KMutableProperty1<out DO, Any?>,
                                       bo: BO, dataObject: DO) {

        if ((boProperty.returnType.classifier as KClass<*>).
                isSubclassOf(BusinessObject::class)
                && doProperty.returnType.classifier == String::class) {
            val value: BusinessObject = getIt(boProperty, bo)
            setIt(doProperty, dataObject, value.id)
        } else if (boProperty.returnType.isSubtypeOf(doProperty.returnType)) {
            val value: Any = getIt(boProperty, bo)
            setIt(doProperty, dataObject, value)
        }
    }


    private fun <T, X> getIt(property: KProperty1<out X, Any?>, obj: X): T =
            (property as KProperty1<X, T>).get(obj)

    private fun <T, X> setIt(doProperty1: KMutableProperty1<out X, Any?>, obj: X, value: T) =
            (doProperty1 as KMutableProperty1<X, T>).set(obj, value)

    fun toBO(dataObject: DO): BO {
        val bo = createBO()
        bo::class.memberProperties.forEach {
            mapPropertyToBo(it, bo, dataObject)
        }
        return bo
    }

    private fun mapPropertyToBo(boProperty: KProperty1<out BO, Any?>, bo: BO, dataObject: DO) {

        val doProperty = dataObject::class.memberProperties.find {
            it.name == boProperty.name || it.name == boProperty.name + "Id"
        }

        if (doProperty != null) {
            if (boProperty is KMutableProperty1) {
                mapMutablePropertyToBo(doProperty, boProperty, dataObject, bo)
            } else {
                throw IllegalStateException("$boProperty is not mutable")
            }
        } else {
            throw IllegalStateException("not matching property found for $boProperty")
        }

    }

    private fun mapMutablePropertyToBo(doProperty: KProperty1<out DO, Any?>, boProperty: KMutableProperty1<out BO, Any?>, dataObject: DO, bo: BO) {
        if (boProperty.returnType.isSubtypeOf(doProperty.returnType)) {
            val value: Any = getIt(doProperty, dataObject)
            setIt(boProperty, bo, value)
        } else if ((boProperty.returnType.classifier as KClass<*>).isSubclassOf(BusinessObject::class) &&
                doProperty.returnType.classifier == String::class) {

            val mapper = mappers.find { it.type == boProperty.returnType.classifier }
            if (mapper == null) {
                throw IllegalStateException("no mapper found for $boProperty")
            } else {
                val value: String = getIt(doProperty, dataObject)
                val mapped = mapper.map(value)
                setIt(boProperty, bo, mapped)
            }


        } else {
            throw IllegalStateException("$doProperty is not mappable to $boProperty")
        }
    }
}


abstract class BusinessObject {
    var id = UUID.randomUUID().toString()
        get
}

class LanguageBusinessObject(var name: String, var isoCode: String) : BusinessObject() {


}
