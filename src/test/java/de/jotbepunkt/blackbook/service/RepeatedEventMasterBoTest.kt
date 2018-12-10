package de.jotbepunkt.blackbook.service

import assertk.all
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.junit.jupiter.api.Test


class RepeatedEventMasterBoTest {

    @Test
    fun `all property values should be taken from the event type if now are overridden`() {
        val bo = repeatedEventMasterBo {
            parent = createEventType()
        }

        assertk.assert(bo).all {
            prop(RepeatedEventMasterBo::comment).isEqualTo(bo.parent?.comment)
            prop(RepeatedEventMasterBo::publicEvent).isEqualTo(true)
            prop(RepeatedEventMasterBo::tags).isEqualTo(bo.parent?.tags)
            prop(RepeatedEventMasterBo::title).isEqualTo(bo.title)
        }
    }

    @Test
    fun `all properties can be overridden`() {

        val tagSet = setOf(tagBo { displayName = "tag 3" })
        val bo = repeatedEventMasterBo {
            parent = createEventType()
            comment = "other comment"
            publicEvent = false
            tags = tagSet
            title = "other title"
        }

        assertk.assert(bo).all {
            prop(RepeatedEventMasterBo::comment).isEqualTo("other comment")
            prop(RepeatedEventMasterBo::publicEvent).isEqualTo(false)
            prop(RepeatedEventMasterBo::tags).isEqualTo(tagSet)
            prop(RepeatedEventMasterBo::title).isEqualTo("other title")
        }
    }

    private fun createEventType(): EventTypeBo {
        return eventTypeBo {
            comment = "comment"
            publicEvent = true
            tags = setOf(
                    tagBo { displayName = "tag1" },
                    tagBo { displayName = "tag2" })
            title = "titile"
        }
    }
}