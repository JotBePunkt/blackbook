@file:Suppress("ClassName")

package de.jotbepunkt.blackbook.service

import assertk.assert
import assertk.assertions.hasClass
import assertk.assertions.isEqualTo
import de.jotbepunkt.blackbook.persistence.Entity
import de.jotbepunkt.blackbook.persistence.EntityRepository
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*


class BusinessServiceTest {

    @Nested
    inner class `BO to DO mapping` {
        @Test
        fun `Simple similar structured BOs can be mapped to DOs`() {
            val testService1 = Basic.Service(mockk())
            val bo = Basic.Bo(bla1 = "blaaaa")
            assert(testService1.toDO(bo)).isEqualTo(Basic.Do(bo.id, "blaaaa"))
        }

        @Test
        fun `Nested similar structured BOs can be mapped to DOs`() {
            val testService2 = NestedBusinessObjects.Service(mockk())
            val nested = Basic.Bo("muh")
            val bo = NestedBusinessObjects.Bo(bla2 = "blubb", nested = nested)
            assert(testService2.toDO(bo)).isEqualTo(NestedBusinessObjects.Do(bo.id, "blubb", nested.id))
        }

        @Test
        fun `Nested list of bos are mapped to Lists of Strings`() {
            val testService3 = NestedSets.Service(mockk(), mockk())
            val nested1 = Basic.Bo("muh1")
            val nested2 = Basic.Bo("muh2")

            val bo = NestedSets.Bo(nested = setOf(nested1, nested2))
            assert(testService3.toDO(bo)).isEqualTo(NestedSets.Do(bo.id, setOf(nested1.id, nested2.id)))
        }

        @Test
        fun `Properties that are not settable leds to errors`() {
            val testService = ImmutableBOProperties.Service(mockk())
            assert {
                testService.toDO(ImmutableBOProperties.Bo("bla"))
            }.thrownError {
                hasClass(NotImplementedError::class)
            }
        }

        @Test
        fun `Ignored Properties are ignored`() {
            val testService = IgnoredProperties.Service(mockk())
            val bo = testService.toBO(IgnoredProperties.Do(prop1 = "test"))

            assert(bo.prop1).isEqualTo("test")
            assert(bo.prop2).isEqualTo("")
        }


        @Test
        fun `properties with underscores are mapped properly`() {

            testMappingBoToDo(
                    PrivateProperties.Service(mockk()),
                    PrivateProperties.Bo().apply { comment = "some comment" }
            ) { dataObject, input ->
                assert(dataObject.comment).isEqualTo(input.comment)
            }
        }

        @Test
        fun `Bos that inherit from generics are properly mapped to Dos`() {
            val basicId = randomUuid()

            testMappingBoToDo(
                    service = Generics.Service(mockk(), mockk()),
                    inputBo = Generics.Bo().apply { nested = Basic.Bo(id = basicId) }) { dataObject, _ ->
                assert(dataObject.nestedId).isEqualTo(basicId)
            }
        }

        private fun <DO : Entity, BO : BusinessObject> testMappingBoToDo(
                service: BusinessService<DO, BO>,
                inputBo: BO,
                expectations: (DO, BO) -> Unit) {

            val dataObject = service.toDO(inputBo)
            expectations(dataObject, inputBo)
        }


    }

    @Nested
    inner class `DO to BO Mapping` {

        @Test
        fun `Simple similar structured Dos can be mapped to BOs`() {
            val testService = Basic.Service(mockk())
            val dataObject = Basic.Do(bla1 = "blaaa")
            assert(testService.toBO(dataObject)).isEqualTo(Basic.Bo(dataObject.id, "blaaa"))
        }

        @Test
        fun `Relations to other BOs are properly resolved`() {

            val testService = NestedBusinessObjects.Service(mockk(), arrayListOf(BusinessService.MapperImpl(Basic.Bo::class, {
                when (it) {
                    "4711" -> Basic.Bo(id = it, bla1 = "blubbs")
                    else -> Basic.Bo(id = it, bla1 = "das wollen wir nicht")
                }
            })))
            val dataObject = NestedBusinessObjects.Do(bla2 = "bla", nestedId = "4711")
            assert(testService.toBO(dataObject)).isEqualTo(NestedBusinessObjects.Bo(dataObject.id, "bla", Basic.Bo(id = "4711", bla1 = "blubbs")))
        }

        @Test
        fun `List of relations to other BOs are propperly mapped`() {
            val testService = NestedSets.Service(mockk(), listOf(BusinessService.MapperImpl(Basic.Bo::class, {
                when (it) {
                    "4711" -> Basic.Bo(id = it, bla1 = "blubbs")
                    "4712" -> Basic.Bo(id = it, bla1 = "muh")
                    else -> Basic.Bo(id = it, bla1 = "das wollen wir nicht")
                }
            })))

            val dataObject = NestedSets.Do(nestedIds = setOf("4711", "4712"))
            assert(testService.toBO(dataObject)).isEqualTo(
                    NestedSets.Bo(dataObject.id, setOf(
                            Basic.Bo("4711", "blubbs"),
                            Basic.Bo("4712", "muh")))
            )

        }

        @Test
        fun `Ignored Properties are ignored`() {
            val testService = IgnoredProperties.Service(mockk())
            val dataObject = testService.toDO(IgnoredProperties.Bo(prop1 = "test", prop2 = "test2"))

            assert(dataObject.prop1).isEqualTo("test")
        }

        @Test
        fun `properties prefixed with underscores are mapped property`() {
            val service = PrivateProperties.Service(mockk())
            val dataObject = service.toDO(
                    PrivateProperties.Bo().apply { comment = "some comment" })

            assert(dataObject.comment).isEqualTo("some comment")
        }

        @Test
        fun `data object are correctly mapped to business object that inherit from Generic class`() {
            val service = Generics.Service(
                    mockk(),
                    listOf(BusinessService.MapperImpl(Basic.Bo::class, { id -> Basic.Bo(id) })))
            val nestedId = randomUuid()
            val bo = service.toBO(Generics.Do(nestedId = nestedId))
            assert(bo.nested.id).isEqualTo(nestedId)
        }
    }

    private fun randomUuid() = UUID.randomUUID().toString()
}

class Basic {

    class Service(override val repo: EntityRepository<Do>,
                  override val mappers: List<BusinessService.MapperImpl<*>> = arrayListOf()) :
            BusinessService<Do, Bo>({ Do() }, { Bo() })

    class Do(id: String = randomId(), var bla1: String = "") : Entity(id) {


        override fun toString(): String {
            return "Do(bla1='$bla1') ${super.toString()}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Do) return false
            if (!super.equals(other)) return false

            if (bla1 != other.bla1) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + bla1.hashCode()
            return result
        }
    }

    class Bo(id: String = randomId(),
             var bla1: String = "") : BusinessObject(id) {
        override fun toString(): String {
            return "Basic.Bo(bla1='$bla1') ${super.toString()}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Basic.Bo) return false
            if (!super.equals(other)) return false

            if (bla1 != other.bla1) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + bla1.hashCode()
            return result
        }
    }
}

class NestedBusinessObjects {

    class Service(override val repo: EntityRepository<Do>,
                  override val mappers: List<BusinessService.MapperImpl<*>> = arrayListOf()) :
            BusinessService<Do, Bo>({ Do() }, { Bo() })

    class Do(id: String = randomId(), var bla2: String = "", var nestedId: String = "") : Entity(id) {


        override fun toString(): String {
            return "Do(bla2='$bla2', nestedId='$nestedId') ${super.toString()}"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Do) return false
            if (!super.equals(other)) return false

            if (bla2 != other.bla2) return false
            if (nestedId != other.nestedId) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + bla2.hashCode()
            result = 31 * result + nestedId.hashCode()
            return result
        }
    }

    class Bo(id: String = randomId(), var bla2: String = "", var nested: Basic.Bo = Basic.Bo()) : BusinessObject(id) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bo) return false
            if (!super.equals(other)) return false

            if (bla2 != other.bla2) return false
            if (nested != other.nested) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + bla2.hashCode()
            result = 31 * result + nested.hashCode()
            return result
        }

        override fun toString(): String {
            return "Bo(bla2='$bla2', nested=$nested) ${super.toString()}"
        }
    }
}

class NestedSets {
    class Service(override val repo: EntityRepository<Do>, override val mappers: List<Mapper<*>>) :
            BusinessService<Do, Bo>({ Do() }, { Bo() })


    class Bo(id: String = randomId(), var nested: Set<Basic.Bo> = setOf()) : BusinessObject(id) {


        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Bo) return false
            if (!super.equals(other)) return false

            if (nested != other.nested) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + nested.hashCode()
            return result
        }

        override fun toString(): String {
            return "Bo(nested=$nested) ${super.toString()}"
        }

    }

    class Do(id: String = randomId(), var nestedIds: Set<String> = setOf()) : Entity(id) {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Do) return false
            if (!super.equals(other)) return false

            if (nestedIds != other.nestedIds) return false

            return true
        }

        override fun hashCode(): Int {
            var result = super.hashCode()
            result = 31 * result + nestedIds.hashCode()
            return result
        }

        override fun toString(): String {
            return "Do(nestedIds=$nestedIds) ${super.toString()}"
        }
    }
}

class ImmutableBOProperties {
    class Service(override val repo: EntityRepository<Do>,
                  override val mappers: List<Mapper<*>> = arrayListOf()) :
            BusinessService<Do, Bo>({ Do() }, { Bo() })

    class Do(val bla: String = "") : Entity(id = randomId())

    class Bo(id: String = randomId(), val bla: String = "") : BusinessObject(id)
}

class IgnoredProperties {
    class Service(override val repo: EntityRepository<Do>)
        : BusinessService<Do, Bo>({ Do() }, { Bo() }) {
        override val mappers: List<Mapper<*>>
            get() = emptyList()
    }

    class Do(
            id: String = randomId(),
            var prop1: String = "")
        : Entity(id)

    class Bo(
            id: String = randomId(),
            var prop1: String = "",
            @IgnoredForMapping var prop2: String = "") : BusinessObject(id)
}


class PrivateProperties {

    class Bo(id: String = randomId()) : BusinessObject(id) {
        private var _comment: String? = null
        var comment: String?
            get() = _comment
            set(value) {
                _comment = value
            }
    }

    class Do(id: String = randomId(), var comment: String? = "") : Entity(id)

    class Service(override val repo: EntityRepository<Do>)
        : BusinessService<Do, Bo>({ Do() }, { Bo() }) {
        override val mappers: List<Mapper<*>> = emptyList()
    }
}

class Generics {
    abstract class BaseBO<T>(id: String = randomId(), var nested: T) : BusinessObject(id) where T : BusinessObject
    class Bo(id: String = randomId()) : BaseBO<Basic.Bo>(id, nested = Basic.Bo())
    class Do(id: String = randomId(), var nestedId: String? = null) : Entity(id)
    class Service(
            override val repo: EntityRepository<Generics.Do>,
            override val mappers: List<Mapper<*>>)
        : BusinessService<Do, Bo>(
            createBO = { Bo() },
            createDO = { Do() })
}
