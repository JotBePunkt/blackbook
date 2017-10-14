package de.jotbepunkt.blackbook.masterdata.eventType

import com.vaadin.data.util.converter.Converter
import com.vaadin.ui.*
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.service.EventTypeBo
import de.jotbepunkt.blackbook.service.EventTypeService
import de.jotbepunkt.blackbook.service.TagBo
import de.jotbepunkt.blackbook.service.TagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import kotlin.reflect.KMutableProperty1

/**
 * Created by bait on 18.07.17.
 */
@Component("eventTypes")
class EventTypeView : MasterDataEditView<EventTypeBo, EventTypeView, EventTypeController>(EventTypeBo::class.java) {

    //  title, comment, tags, publicEvent
    val titleField = TextField("Title")
    val comment = TextArea("comment")
    val tags = TwinColSelect("tags")
    val publicEvent = CheckBox("public")

    override val formElements: Map<KMutableProperty1<EventTypeBo, out Any>, Field<out Any>>
        get() = mapOf(
                EventTypeBo::title to titleField,
                EventTypeBo::comment to comment,
                EventTypeBo::tags to tags,
                EventTypeBo::publicEvent to publicEvent
        )

    init {
        tags.isMultiSelect = true
        tags.setConverter(object : Converter<Set<TagBo>, List<TagBo>> {
            override fun getModelType(): Class<List<TagBo>> {
                return List::class.java as Class<List<TagBo>>
            }

            override fun convertToPresentation(value: List<TagBo>?, targetType: Class<out Set<TagBo>>?, locale: Locale?): Set<TagBo> {
                return value!!.toHashSet()
            }

            override fun convertToModel(value: Set<TagBo>?, targetType: Class<out List<TagBo>>?, locale: Locale?): List<TagBo> {
                return value!!.toList()
            }

            override fun getPresentationType(): Class<Set<TagBo>> {
                return Set::class.java as Class<Set<TagBo>>
            }
        } as Converter<Any, *>)
    }

}

@Component
class EventTypeController
@Autowired constructor(view: EventTypeView, override val dataService: EventTypeService)
    : MasterDataEditController<EventTypeBo, EventTypeView, EventTypeController>(view) {

    @Autowired lateinit var tagService: TagService

    override fun onShow() {
        view.tags.removeAllItems()
        view.tags.addItems(tagService.findAll())
    }
}
