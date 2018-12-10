package de.jotbepunkt.blackbook.service

import assertk.assert
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isSameAs
import assertk.assertions.prop
import de.jotbepunkt.blackbook.persistence.RepeatedEventMaster
import de.jotbepunkt.blackbook.persistence.RepeatedEventMasterRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDate

class RepeatedEventMasterServiceTest {

    private val repo = mockk<RepeatedEventMasterRepository>(relaxed = true)
    private val eventTypeService = mockk<EventTypeService>(relaxed = true)
    private val tagService = mockk<TagService>(relaxed = true)
    private val repeatedEventService = mockk<RepeatedEventService>()


    val savedRepeatedEvents = mutableListOf<RepeatedEventInstanceBo>()


    private val repeatedEventMasterService = RepeatedEventMasterService(
            repo = repo,
            eventTypeService = eventTypeService,
            tagService = tagService,
            repeatedEventService = repeatedEventService)

    @Nested
    inner class GeneralTests {

        @Test
        fun `save should create a single event when the end date is the start date`() {

            val master = repeatedEventMasterBo {
                date = LocalDate.now()
                endDate = LocalDate.now()
            }

            repeatedEventMasterService.save(master)

            verify {
                repeatedEventService.save(any())
            }

            assert(savedRepeatedEvents).hasSize(1)
            assert(savedRepeatedEvents[0])
                    .prop(RepeatedEventInstanceBo::parent)
                    .isSameAs(master)

            assert(savedRepeatedEvents[0])
                    .prop(RepeatedEventInstanceBo::date)
                    .isEqualTo(master.date)

        }
    }

    @Nested
    inner class `Daily Events` {

        @Test
        fun `daily event occurs 8 times from today until the same day in a week`() {
            val master = repeatedEventMasterBo {
                date = LocalDate.now()
                endDate = LocalDate.now().plusWeeks(1)
                daily {
                    every = 1
                }
            }

            repeatedEventMasterService.save(master)

            assert(savedRepeatedEvents).hasSize(8)
            savedRepeatedEvents.forEachIndexed { index, it ->
                assert(it, "element $index")
                        .prop(RepeatedEventInstanceBo::parent)
                        .isEqualTo(master)
                assert(it, "element $index")
                        .prop(RepeatedEventInstanceBo::date)
                        .isEqualTo(LocalDate.now().plusDays(index.toLong()))
            }
        }

        @Test
        fun `daily event occurs 4 times from today until the same day in a week when it is sceduled every second day`() {
            val master = repeatedEventMasterBo {
                date = LocalDate.now()
                endDate = LocalDate.now().plusWeeks(1)
                daily {
                    every = 2
                }
            }

            repeatedEventMasterService.save(master)

            assert(savedRepeatedEvents).hasSize(4)
            savedRepeatedEvents.forEachIndexed { index, it ->
                assert(it, "element $index")
                        .prop(RepeatedEventInstanceBo::parent)
                        .isEqualTo(master)
                assert(it, "element $index")
                        .prop(RepeatedEventInstanceBo::date)
                        .isEqualTo(LocalDate.now().plusDays(index.toLong() * 2))
            }
        }
    }

    @Nested
    inner class `Weekly Events` {
//
//        @Test
//        fun `save createas an event for every week when the event is weekly and over one month`() {
//
//            fail("not yet implemented")
//        }
    }

    @Nested
    inner class `Monthly Events`


    @BeforeEach
    fun initMapperBehaviour() {
        every { eventTypeService.type } returns EventTypeBo::class
        every { eventTypeService.mapSingle } returns { EventTypeBo() }
        every { tagService.type } returns TagBo::class
        every { tagService.mapSingle } returns { TagBo() }
        every { tagService.mapMultiple } returns { setOf(TagBo()) }
    }

    @BeforeEach
    fun initRepeatedEventServiceBehaviour() {
        savedRepeatedEvents.clear()
        every {
            repeatedEventService.save(capture(savedRepeatedEvents))
        } answers {
            savedRepeatedEvents.captured()
        }
    }

    @BeforeEach
    fun initRepoBehaviour() {
        val storedDataObjects = mutableListOf<RepeatedEventMaster>()
        every {
            repo.save(capture(storedDataObjects))
        } answers {
            storedDataObjects.captured()
        }
    }

}

