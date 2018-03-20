package de.jotbepunkt.blackbook.masterdata.eventType

import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.CheckBox
import com.vaadin.ui.TextArea
import com.vaadin.ui.TextField
import com.vaadin.ui.TwinColSelect
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.service.EventTypeBo
import de.jotbepunkt.blackbook.service.EventTypeService
import de.jotbepunkt.blackbook.service.TagBo
import de.jotbepunkt.blackbook.service.TagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by bait on 18.07.17.
 */
@SpringView(name = "eventTypes")
class EventTypeView
@Autowired constructor(controller: EventTypeController) : MasterDataEditView<EventTypeBo, EventTypeView, EventTypeController>(EventTypeBo::class.java, controller) {

    //  title, comment, tags, publicEvent
    private val titleField = TextField("Title")
    private val comment = TextArea("comment")
    internal val tags = TwinColSelect<TagBo>("tags")
    private val publicEvent = CheckBox("public")

    override val formElements: List<Binding<EventTypeBo, *>>
        get() = bind(
                EventTypeBo::title to titleField,
                EventTypeBo::comment to comment,
                EventTypeBo::tags to tags,
                EventTypeBo::publicEvent to publicEvent
        )
}

@Component
@ViewScope
class EventTypeController
@Autowired constructor(override val dataService: EventTypeService)
    : MasterDataEditController<EventTypeBo, EventTypeView, EventTypeController>() {

    @Autowired lateinit var tagService: TagService

    override fun onShow() {
        view.tags.setItems(tagService.findAll())
    }
}
