package de.jotbepunkt.blackbook.service

import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.mock.mock
import io.kotlintest.specs.StringSpec
import de.jotbepunkt.blackbook.persistence.Entity
import de.jotbepunkt.blackbook.persistence.EntityRepository
import org.springframework.data.mongodb.repository.MongoRepository


class BoToDoMapppingServicesSpecs : StringSpec() {
    init {
        "Simple similar structured BOs can be mapped to DOs" {
            val testService1 = TestService1(mock())
            val bo = TestBO1(bla1 = "blaaaa")
            testService1.toDO(bo) shouldEqual TestDO1(bo.id, "blaaaa")
        }

        "Nested similar structured BOs can be mapped to DOs" {
            val testService2 = TestService2(mock())
            val nested = TestBO1("muh")
            val bo = TestBO2(bla2 = "blubb", nested = nested)
            testService2.toDO(bo) shouldEqual TestDO2(bo.id, "blubb", nested.id)
        }

        "Nested list of bos are mapped to Lists of Strings" {
            val testService3 = TestService3(mock(), mock())
            val nested1 = TestBO1("muh1")
            val nested2 = TestBO1("muh2")

            val bo = TestBO3(nested = arrayListOf(nested1, nested2))
            testService3.toDO(bo) shouldEqual TestDO3(bo.id, arrayListOf(nested1.id, nested2.id))
        }

        "Properties that are not settable leds to errors" {
            val testService = TestServiceOfImutableBo(mock())

            shouldThrow<NotImplementedError> {
                testService.toDO(BoOfImutableDo("bla"))
            }
        }

        "Ignored Properties are ignored" {
            val testService = TestService4(mock())
            val bo = testService.toBO(TestDo4(prop1 = "test"))

            bo.prop1 shouldEqual "test"
            bo.prop2 shouldEqual ""
        }
    }
}

class DoToBoMapping : StringSpec() {
    init {
        "Simple similar structured Dos can be mapped to BOs" {
            val testService = TestService1(mock())
            val dataObject = TestDO1(bla1 = "blaaa")
            testService.toBO(dataObject) shouldEqual TestBO1(dataObject.id, "blaaa")
        }

        "Relations to other BOs are properly resolved" {

            val testService = TestService2(mock(), arrayListOf(BusinessService.MapperImpl(TestBO1::class, {
                when (it) {
                    "4711" -> TestBO1(id = it, bla1 = "blubbs")
                    else -> TestBO1(id = it, bla1 = "das wollen wir nicht")
                }
            })))
            val dataObject = TestDO2(bla2 = "bla", nestedId = "4711")
            testService.toBO(dataObject) shouldEqual TestBO2(dataObject.id, "bla", TestBO1(id = "4711", bla1 = "blubbs"))
        }

        "List of relations to other BOs are propperly mapped" {
            val testService = TestService3(mock(), arrayListOf(BusinessService.MapperImpl(TestBO1::class, {
                when (it) {
                    "4711" -> TestBO1(id = it, bla1 = "blubbs")
                    "4712" -> TestBO1(id = it, bla1 = "muh")
                    else -> TestBO1(id = it, bla1 = "das wollen wir nicht")
                }
            })))

            val dataObject = TestDO3(nestedIds = arrayListOf("4711", "4712"))
            testService.toBO(dataObject) shouldEqual
                    TestBO3(dataObject.id, arrayListOf(
                            TestBO1("4711", "blubbs"),
                            TestBO1("4712", "muh")))

        }

        "Ignored Properties are ignored" {
            val testService = TestService4(mock())
            val dataObject = testService.toDO(TestBo4(prop1 = "test", prop2 = "test2"))

            dataObject.prop1 shouldEqual "test"
        }
    }

}

class TestService1(override val repo: EntityRepository<TestDO1>,
                   override val mappers: List<BusinessService.MapperImpl<*>> = arrayListOf()) :
        BusinessService<TestDO1, TestBO1>({ TestDO1() }, { TestBO1() })

class TestService2(override val repo: EntityRepository<TestDO2>,
                   override val mappers: List<BusinessService.MapperImpl<*>> = arrayListOf()) :
        BusinessService<TestDO2, TestBO2>({ TestDO2() }, { TestBO2() })


class TestBO1(id: String = randomId(),
              var bla1: String = "") : BusinessObject(id) {
    override fun toString(): String {
        return "TestBO1(bla1='$bla1') ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestBO1) return false
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

class TestBO3(id: String = randomId(), var nested: List<TestBO1> = arrayListOf()) : BusinessObject(id) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestBO3) return false
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
        return "TestBO3(nested=$nested) ${super.toString()}"
    }

}

class TestDO3(id: String = randomId(), var nestedIds: List<String> = arrayListOf()) : Entity(id) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestDO3) return false
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
        return "TestDO3(nestedIds=$nestedIds) ${super.toString()}"
    }

}

class TestService3(override val repo: EntityRepository<TestDO3>, override val mappers: List<Mapper<*>>) :
        BusinessService<TestDO3, TestBO3>({ TestDO3() }, { TestBO3() })


class TestBO2(id: String = randomId(), var bla2: String = "", var nested: TestBO1 = TestBO1()) : BusinessObject(id) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestBO2) return false
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
        return "TestBO2(bla2='$bla2', nested=$nested) ${super.toString()}"
    }


}

class TestDO2(id: String = randomId(), var bla2: String = "", var nestedId: String = "") : Entity(id) {


    override fun toString(): String {
        return "TestDO2(bla2='$bla2', nestedId='$nestedId') ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestDO2) return false
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

class TestDO1(id: String = randomId(), var bla1: String = "") : Entity(id) {


    override fun toString(): String {
        return "TestDO1(bla1='$bla1') ${super.toString()}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TestDO1) return false
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


class TestServiceOfImutableBo(override val repo: EntityRepository<ImutableDO>,
                              override val mappers: List<Mapper<*>> = arrayListOf()) :
        BusinessService<ImutableDO, BoOfImutableDo>({ ImutableDO() }, { BoOfImutableDo() })

class ImutableDO(val bla: String = "") : Entity(id = randomId())

class BoOfImutableDo(id: String = randomId(), val bla: String = "") : BusinessObject(id)

class TestDo4(
        id: String = randomId(),
        var prop1: String = "")
    : Entity(id)

class TestBo4(
        id: String = randomId(),
        var prop1: String = "",
        @IgnoredForMapping var prop2: String = "") : BusinessObject(id)

class TestService4(override val repo: EntityRepository<TestDo4>)
    : BusinessService<TestDo4, TestBo4>({ TestDo4() }, { TestBo4() }) {
    override val mappers: List<Mapper<*>>
        get() = emptyList()
}
