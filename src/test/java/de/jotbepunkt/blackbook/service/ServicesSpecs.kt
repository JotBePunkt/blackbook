package de.jotbepunkt.blackbook.service

import de.jotbepunkt.blackbook.masterdata.Entity
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldThrow
import io.kotlintest.mock.mock
import io.kotlintest.specs.StringSpec
import org.springframework.data.mongodb.repository.MongoRepository


class BoToDoMapppingServicesSpecs : StringSpec() {
    init {

        "Simple similar structured BOs can be mapped to DOs" {
            val testService1 = TestService1(mock())
            testService1.toDO(TestBO1("blaaaa")) shouldEqual TestDO1("blaaaa")
        }

        "Nested similar structured BOs can be mapped to DOs" {
            val testService2 = TestService2(mock())
            val nested = TestBO1("muh")
            testService2.toDO(TestBO2("blubb", nested)) shouldEqual TestDO2("blubb", nested.id)
        }

        "Properties that are not settable leds to errors" {
            val testService = TestServiceOfUnmutableBo(mock())

            shouldThrow<IllegalStateException> {
                testService.toDO(BoOfInmutableDo("bla"))
            }
        }
    }
}

class DoToBoMapping : StringSpec() {
    init {
        "Simple simplar structured Dos can be mapped to BOs" {
            val testService = TestService1(mock())
            testService.toBO(TestDO1("blaaaa")) shouldEqual TestBO1("blaaaa")
        }

        "Relations to other BOs are properly resolved" {

            val testService = TestService2(mock(), arrayListOf(Service.Mapper(TestBO1::class, {
                if (it == "4711") {
                    TestBO1("blubbs")
                } else {
                    TestBO1("das wollen wir nicht")
                }
            })))
            testService.toBO(TestDO2("bla", "4711")) shouldEqual TestBO2("bla", TestBO1("blubbs"));
        }
    }

}

class TestService1(override val repo: MongoRepository<TestDO1, String>,
                   override val mappers: List<Mapper<*>> = arrayListOf()) :
        Service<TestDO1, TestBO1>({ TestDO1() }, { TestBO1() })

class TestService2(override val repo: MongoRepository<TestDO2, String>,
                   override val mappers: List<Mapper<*>> = arrayListOf()) :
        Service<TestDO2, TestBO2>({ TestDO2() }, { TestBO2() })


class TestBO1(var bla1: String = "") : BusinessObject() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TestBO1

        if (bla1 != other.bla1) return false

        return true
    }

    override fun hashCode(): Int {
        return bla1.hashCode()
    }

    override fun toString(): String {
        return "TestBO1(bla1='$bla1')"
    }


}

class TestBO2(var bla2: String = "", var nested: TestBO1 = TestBO1()) : BusinessObject() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TestBO2

        if (bla2 != other.bla2) return false
        if (nested != other.nested) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bla2.hashCode()
        result = 31 * result + nested.hashCode()
        return result
    }
}

class TestDO2(var bla2: String = "", var nestedId: String = "") : Entity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TestDO2

        if (bla2 != other.bla2) return false
        if (nestedId != other.nestedId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bla2.hashCode()
        result = 31 * result + nestedId.hashCode()
        return result
    }

    override fun toString(): String {
        return "TestDO2(bla2='$bla2', nestedId='$nestedId')"
    }


}

class TestDO1(var bla1: String = "") : Entity() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as TestDO1

        if (bla1 != other.bla1) return false

        return true
    }

    override fun hashCode(): Int {
        return bla1.hashCode()
    }

    override fun toString(): String {
        return "TestDO1(bla1='$bla1')"
    }
}


class TestServiceOfUnmutableBo(override val repo: MongoRepository<InmutableDO, String>,
                               override val mappers: List<Mapper<*>> = arrayListOf()) :
        Service<InmutableDO, BoOfInmutableDo>({ InmutableDO() }, { BoOfInmutableDo() })

class InmutableDO(val bla: String = "") : Entity() {
}

class BoOfInmutableDo(val bla: String = "") : BusinessObject() {

}