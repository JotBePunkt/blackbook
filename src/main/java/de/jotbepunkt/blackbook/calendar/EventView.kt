package de.jotbepunkt.blackbook.calendar

import com.vaadin.data.HasValue
import com.vaadin.navigator.View
import com.vaadin.shared.ui.datefield.DateTimeResolution
import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.*
import de.jotbepunkt.blackbook.service.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZonedDateTime

@SpringView(name = "event")
@Component
@ViewScope
class EventView
@Autowired constructor(private val controller: EventController) :
        GridLayout(3, 9), View {

    val window = Window()

    val eventTypeComboBox = ComboBox<EventTypeBo>().apply {
        addValueChangeListener {
            setViewState()
        }
        this.isEmptySelectionAllowed = false
    }

    val fromDate = DateTimeField().apply {
        resolution = DateTimeResolution.MINUTE
    }

    val toDate = DateTimeField().apply {
        resolution = DateTimeResolution.MINUTE
    }

    val titleText = TextField()
    val commentArea = TextArea()
    val tagsSelect = TwinColSelect<TagBo>()
    val publicCheckBox = CheckBox()

    val titleOverride = CheckBox().applyOverrideHandler(titleText, "")
    val commentOverride = CheckBox().applyOverrideHandler(commentArea, "")
    val tagsOverride = CheckBox().applyOverrideHandler(tagsSelect, setOf())
    val publicOverride = CheckBox().applyOverrideHandler(publicCheckBox, false)

    private fun <T> CheckBox.applyOverrideHandler(field: HasValue<T>, neutralValue: T): CheckBox {
        this.addValueChangeListener { event ->
            setViewState()
            if (value == null) {
                field.value = neutralValue
            }
        }
        return this
    }

    //    val saveButton = Button("save").apply {
//        addClickListener { controller.save(bo) }
//    }
    val cancelButton = Button("cancel")

    //val binder = BeanValidationBinder<EventBo<EventTypeBo>>(EventBo::class.java)

//    var bo: EventBo<EventTypeBo> = SingleEventBo()
//        set(value) {
//            binder.readBean(value)
//            setViewState()
//        }
//        get() {
//            binder.writeBean(field)
//            return field
//        }

    init {
        controller.view = this

        setMargin(true)
        isSpacing = true

        var row = 1
        addComponent(Label("from"), 0, row++)
        addComponent(Label("to"), 0, row++)
        addComponent(Label("type"), 0, row++)
        addComponent(Label("title"), 0, row++)
        addComponent(Label("comment"), 0, row++)
        addComponent(Label("tags"), 0, row++)
        addComponent(Label("public event"), 0, row)

        row = 4
        addComponent(Label("overwrite"), 1, 0)
        addComponent(titleOverride, 1, row++)
        addComponent(commentOverride, 1, row++)
        addComponent(tagsOverride, 1, row++)
        addComponent(publicOverride, 1, row)

        row = 1
        addComponent(fromDate, 2, row++)
        addComponent(toDate, 2, row++)
        addComponent(eventTypeComboBox, 2, row++)
        addComponent(titleText, 2, row++)
        addComponent(commentArea, 2, row++)
        addComponent(tagsSelect, 2, row++)
        addComponent(publicCheckBox, 2, row++)

//        addComponent(HorizontalLayout(cancelButton, saveButton).apply {
//            setWidth("100%")
//            setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT)
//            setExpandRatio(cancelButton, 1f)
//        }, 0, row, 2, row)

//        binder.bind(
//                EventBo<EventTypeBo>::start to fromDate,
//                EventBo<EventTypeBo>::end to toDate,
//                EventBo<EventTypeBo>::parent to eventTypeComboBox,
//                EventBo<EventTypeBo>::title to titleText,
//                EventBo<EventTypeBo>::comment to commentArea,
//                EventBo<EventTypeBo>::tags to tagsSelect,
//                EventBo<EventTypeBo>::publicEvent to publicCheckBox,
//
//                EventBo<EventTypeBo>::overWriteTitle to titleOverride,
//                EventBo<EventTypeBo>::overWriteComment to commentOverride,
//                EventBo<EventTypeBo>::overWriteTags to tagsOverride,
//                EventBo<EventTypeBo>::overWritePublicEvent to publicOverride
//        )
    }

    fun show(caption: String) {
        window.caption = caption
        window.content = this
        window.center()
        UI.getCurrent().addWindow(window)
    }

    fun unshow() {
        UI.getCurrent().removeWindow(window)
    }

    fun setViewState() {
        listOf<AbstractComponent>(
                titleOverride,
                commentOverride,
                tagsOverride,
                publicOverride,
                titleText,
                commentArea,
                tagsSelect,
                publicCheckBox).forEach { element -> element.isEnabled = eventTypeComboBox.value != null }

        titleText.isReadOnly = !titleOverride.value
        commentArea.isReadOnly = !commentOverride.value
        tagsSelect.isReadOnly = !tagsOverride.value
        publicCheckBox.isReadOnly = !publicOverride.value

        // reread the bean so we get the non-overridden values
//        binder.writeBean(bo)
//        binder.readBean(bo)
    }


}

@Component
@ViewScope
class EventController
@Autowired constructor(val singleEventService: SingleEventService,
                       val eventTypeService: EventTypeService,
                       val tagService: TagService) {
    fun showNewEvent(start: ZonedDateTime, end: ZonedDateTime) {
        initSelects()

        val bo = SingleEventBo().apply {
            this.start = LocalDateTime.from(start)
            this.end = LocalDateTime.from(end)
        }

//        view.bo = bo
        view.show("New event")
    }

    private fun initSelects() {
        view.eventTypeComboBox.setItems(eventTypeService.findAll())
        view.tagsSelect.setItems(tagService.findAll())
    }

    fun save(bo: EventBo<*>) {
        singleEventService.save(bo as SingleEventBo)
        view.unshow()
    }

    fun editEvent(id: String) {
        val eventBo = singleEventService.find(id)!!
//        view.bo = eventBo
        initSelects()
        view.show("Edit event")
    }

    lateinit var view: EventView
}
