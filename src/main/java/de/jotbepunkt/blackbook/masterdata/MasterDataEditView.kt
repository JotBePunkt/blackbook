package de.jotbepunkt.blackbook.masterdata

import com.vaadin.data.BeanValidationBinder
import com.vaadin.data.ValidationException
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.*
import de.jotbepunkt.blackbook.service.BusinessObject
import de.jotbepunkt.blackbook.service.BusinessService
import de.jotbepunkt.blackbook.vaadin.Binding
import de.jotbepunkt.blackbook.vaadin.bind

/**
 * Parent for Master data edit views
 */
abstract class MasterDataEditView<BO : BusinessObject, V : MasterDataEditView<BO, V, C>,
        C : MasterDataEditController<BO, V, C>>(boClass: Class<BO>, private var controller: C) : HorizontalLayout(), View {

    // that is just syntactical sugar
    fun bind(vararg bindings: Binding<BO, out Any>) = listOf(*bindings)

    private val listSelect = ListSelect<BO>()
    private val saveButton = Button("Save")
    private val addButton = Button("+")
    private val removeButton = Button("-")

    abstract val formElements: List<Binding<BO, out Any>>

    private val formLayout = FormLayout()

    private val binder = BeanValidationBinder<BO>(boClass)

    var isEditorEnabled: Boolean = false
        set(value) {
            saveButton.isEnabled = value
            formElements
                    .map { it.field as Component }
                    .forEach {
                        it.isEnabled = value
                    }
            field = value
        }


    init {
        @Suppress("LeakingThis", "UNCHECKED_CAST")
        controller.view = this as V
        addButton.addClickListener { controller.addElement() }
        removeButton.addClickListener { controller.removeElement() }
        saveButton.addClickListener { controller.saveElement() }

        listSelect.addValueChangeListener {
            controller.currentBusinessObject = listSelect.selectedItems.firstOrNull()
        }

        listSelect.setHeight("100%")
    }

    override fun attach() {
        initUi()
        super.attach()
        controller.onShow()
    }

    private fun initUi() {
        formElements.forEach { formLayout.addComponent(it.field as Component) }
        formLayout.addComponent(saveButton)

        val buttons = HorizontalLayout(addButton, removeButton)
        buttons.setExpandRatio(addButton, 1f)
        buttons.setExpandRatio(removeButton, 1f)

        val selectArea = VerticalLayout(listSelect, buttons)

        addComponents(selectArea, formLayout)

        setComponentAlignment(formLayout, Alignment.MIDDLE_CENTER)

        setExpandRatio(selectArea, 1f)
        setExpandRatio(formLayout, 2f)

        setHeight("100%")
        setWidth("100%")

        binder.bind(formElements)
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        controller.loadElements()
        isEditorEnabled = false
    }

    @Suppress("UNCHECKED_CAST")
    fun getSelectedElement(): BO? = listSelect.value.firstOrNull()

    fun setItems(elements: List<BO>) =
            listSelect.setItems(elements)


    fun readBo(bo: BO) =
            binder.readBean(bo)

    @Throws(ValidationException::class)
    fun writeBo(bo: BO) =
            binder.writeBean(bo)
}


abstract class MasterDataEditController<BO : BusinessObject, V : MasterDataEditView<BO, V, C>, C : MasterDataEditController<BO, V, C>> {

    abstract val dataService: BusinessService<*, BO>
    lateinit var view: V

    var currentBusinessObject: BO? = null
        set(value) {
            if (value != null) {
                view.readBo(value)
                view.isEditorEnabled = true
            } else {
                view.readBo(createElement())
                view.isEditorEnabled = false
            }
            field = value
        }
        get() = if (field != null) {
            view.writeBo(field!!)
            field
        } else {
            null
        }


    private fun createElement(): BO {
        return dataService.createBO()
    }

    fun addElement() {
        currentBusinessObject = createElement()
    }

    fun removeElement() {
        val selected = view.getSelectedElement()
        if (selected != null) {
            dataService.delete(selected)
        }

        loadElements()
        currentBusinessObject = null

    }

    fun loadElements() {
        view.setItems(dataService.findAll())
    }

    fun saveElement() {
        dataService.save(currentBusinessObject!!)
        loadElements()
        currentBusinessObject = null
    }

    /**
     * called when the view is shown at the UI. Can be used to initialize the UI
     */

    open fun onShow() {
    }
}

