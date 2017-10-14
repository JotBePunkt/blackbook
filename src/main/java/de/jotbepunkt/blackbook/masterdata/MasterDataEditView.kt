package de.jotbepunkt.blackbook.masterdata

import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.data.validator.BeanValidator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.*
import de.jotbepunkt.blackbook.service.BusinessObject
import de.jotbepunkt.blackbook.service.BusinessService
import kotlin.reflect.KMutableProperty1

/**
 * Created by bait on 07.07.17.
 */
abstract class MasterDataEditView<BO : BusinessObject, V : MasterDataEditView<BO, V, C>,
        C : MasterDataEditController<BO, V, C>>(val boClass: Class<BO>) : HorizontalLayout(), View {

    lateinit var controller: C

    private val listSelect = ListSelect()
    private val saveButton = Button("Save")
    private val addButton = Button("+")
    private val removeButton = Button("-")

    abstract val formElements: Map<KMutableProperty1<BO, out Any>, Field<out Any>>

    private val formLayout = FormLayout()

    val fieldGroup = BeanFieldGroup(boClass)

    var isEditorEnabled: Boolean = false
        set(value) {
            formLayout.isEnabled = value
            if (!value) {
                fieldGroup.setItemDataSource(controller.createElement())
            }
        }

    init {
        addButton.addClickListener { controller.addElement() }
        removeButton.addClickListener { controller.removeElement() }
        saveButton.addClickListener { controller.saveElement() }

        listSelect.addValueChangeListener {
            if (listSelect.value != null) {
                @Suppress("UNCHECKED_CAST")
                controller.elementSelected(listSelect.value as BO)
            }
        }

        listSelect.setHeight("100%")
    }

    override fun attach() {
        initUi()
        super.attach()
        controller.onShow()
    }

    private fun initUi() {
        formElements.values.forEach { formLayout.addComponent(it) }
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

        bindFields()
    }

    private fun bindFields() {
        formElements.forEach { property, field ->
            fieldGroup.bind(field, property.name)
            field.addValidator(BeanValidator(boClass, property.name))
        }
    }


    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        controller.loadElements()
        isEditorEnabled = false
    }

    @Suppress("UNCHECKED_CAST")
    fun getSelectedElement(): BO? = listSelect.value as BO?

    fun setItems(elements: List<BO>) {
        listSelect.removeAllItems()
        listSelect.addItems(elements)
    }

    fun getEditedElement(): BO {

        fieldGroup.commit()
        return fieldGroup.itemDataSource.bean
    }


}

@Suppress("UNCHECKED_CAST")
abstract class MasterDataEditController<BO : BusinessObject, V : MasterDataEditView<BO, V, C>, C : MasterDataEditController<BO, V, C>>(val view: V) {

    abstract val dataService: BusinessService<*, BO>

    init {
        @Suppress("LeakingThis")
        view.controller = this as C
    }

    fun createElement(): BO {
        return dataService.createBO()
    }

    fun addElement() {
        view.fieldGroup.setItemDataSource(createElement())
        view.isEditorEnabled = true

    }

    fun removeElement() {
        val selected = view.getSelectedElement()
        if (selected != null) {
            dataService.delete(selected)
        }

        loadElements()
        view.isEditorEnabled = false
    }

    fun loadElements() {
        view.setItems(dataService.findAll())
    }

    fun saveElement() {
        val edited = view.getEditedElement()

        dataService.save(edited)

        loadElements()
        view.isEditorEnabled = false
    }

    fun elementSelected(value: BO) {
        view.fieldGroup.setItemDataSource(value)
        view.isEditorEnabled = true
    }

    /**
     * called when the view is shown at the UI. Can be used to initialize the UI
     */

    open fun onShow() {
    }

}

