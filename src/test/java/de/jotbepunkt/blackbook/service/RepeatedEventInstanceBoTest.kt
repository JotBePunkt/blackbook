package de.jotbepunkt.blackbook.service

import assertk.all
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalTime

internal class RepeatedEventInstanceBoTest {

    @Test
    fun `all values are taken from the eventType if neither master nor event overwrites it`() {

        val eventTypeTags = setOf(tagBo { displayName = "eventTypeTag" })
        val bo = repeatedEventInstanceBo {

            parent = repeatedEventMasterBo {
                parent = eventTypeBo {
                    title = "eventTypeTitle"
                    comment = "eventTypeComment"
                    tags = eventTypeTags
                    publicEvent = true
                }
            }
        }

        assert(bo).all {
            prop(RepeatedEventInstanceBo::title).isEqualTo("eventTypeTitle")
            prop(RepeatedEventInstanceBo::comment).isEqualTo("eventTypeComment")
            prop(RepeatedEventInstanceBo::tags).isEqualTo(eventTypeTags)
            prop(RepeatedEventInstanceBo::publicEvent).isEqualTo(true)
        }
    }

    @Test
    fun `all values are taken from the master if they are overridden there`() {
        val eventTypeTags = setOf(tagBo { displayName = "eventTypeTag" })
        val masterTags = setOf(tagBo { displayName = "masterTag" })
        val bo = repeatedEventInstanceBo {

            parent = repeatedEventMasterBo {
                parent = eventTypeBo {
                    title = "eventTypeTitle"
                    comment = "eventTypeComment"
                    tags = eventTypeTags
                    publicEvent = true
                }
                title = "masterTitle"
                comment = "masterComment"
                tags = masterTags
                publicEvent = false
            }
        }

        assert(bo).all {
            prop(RepeatedEventInstanceBo::title).isEqualTo("masterTitle")
            prop(RepeatedEventInstanceBo::comment).isEqualTo("masterComment")
            prop(RepeatedEventInstanceBo::tags).isEqualTo(masterTags)
            prop(RepeatedEventInstanceBo::publicEvent).isEqualTo(false)
        }
    }

    @Test
    fun `all values are taken from the event if they are overridden there`() {
        val eventTypeTags = setOf(tagBo { displayName = "eventTypeTag" })
        val masterTags = setOf(tagBo { displayName = "masterTag" })
        val eventTags = setOf(tagBo { displayName = "eventTags" })

        val bo = repeatedEventInstanceBo {
            parent = repeatedEventMasterBo {
                parent = eventTypeBo {
                    title = "eventTypeTitle"
                    comment = "eventTypeComment"
                    tags = eventTypeTags
                    publicEvent = true
                }
                title = "masterTitle"
                comment = "masterComment"
                tags = masterTags
                publicEvent = true
            }

            title = "eventTitle"
            comment = "eventComment"
            tags = eventTags
            publicEvent = false
        }

        assert(bo).all {
            prop(RepeatedEventInstanceBo::title).isEqualTo("eventTitle")
            prop(RepeatedEventInstanceBo::comment).isEqualTo("eventComment")
            prop(RepeatedEventInstanceBo::tags).isEqualTo(eventTags)
            prop(RepeatedEventInstanceBo::publicEvent).isEqualTo(false)
        }
    }

    @Test
    fun `startTime and duration are taken from master if not overwritten`() {
        val masterStartTime = LocalTime.now()

        val bo = repeatedEventInstanceBo {
            parent = repeatedEventMasterBo {
                startTime = masterStartTime
                length = Duration.ofHours(3)
            }
        }
        assert(bo).all {
            prop(RepeatedEventInstanceBo::startTime).isEqualTo(masterStartTime)
            prop(RepeatedEventInstanceBo::length).isEqualTo(Duration.ofHours(3))
        }
    }

    @Test
    fun `startTime and duration can be overwritten`() {
        val masterStartTime = LocalTime.now()
        val eventStartTime = LocalTime.now().plusHours(3)

        val bo = repeatedEventInstanceBo {
            parent = repeatedEventMasterBo {
                startTime = masterStartTime
                length = Duration.ofHours(3)
            }
            startTime = eventStartTime
            length = Duration.ofHours(1)
        }

        assert(bo).all {
            prop(RepeatedEventInstanceBo::startTime).isEqualTo(eventStartTime)
            prop(RepeatedEventInstanceBo::length).isEqualTo(Duration.ofHours(1))
        }
    }
}