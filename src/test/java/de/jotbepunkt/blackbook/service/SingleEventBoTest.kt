package de.jotbepunkt.blackbook.service

import assertk.all
import assertk.assert
import assertk.assertions.isEqualTo
import assertk.assertions.prop
import org.junit.jupiter.api.Test

class SingleEventBoTest {

    @Test
    fun `all property values should be taken from the event type if now are overridden`() {
        val bo = singleEventBo {
            parent = createEventType()
        }

        assert(bo).all {
            prop(SingleEventBo::comment).isEqualTo(bo.parent?.comment)
            prop(SingleEventBo::publicEvent).isEqualTo(true)
            prop(SingleEventBo::tags).isEqualTo(bo.parent?.tags)
            prop(SingleEventBo::title).isEqualTo(bo.title)
        }
    }

    @Test
    fun `all properties can be overridden`() {

        val tagSet = setOf(tagBo { displayName = "tag 3" })
        val bo = singleEventBo {
            parent = createEventType()
            comment = "other comment"
            publicEvent = false
            tags = tagSet
            title = "other title"
        }

        assert(bo).all {
            prop(SingleEventBo::comment).isEqualTo("other comment")
            prop(SingleEventBo::publicEvent).isEqualTo(false)
            prop(SingleEventBo::tags).isEqualTo(tagSet)
            prop(SingleEventBo::title).isEqualTo("other title")
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