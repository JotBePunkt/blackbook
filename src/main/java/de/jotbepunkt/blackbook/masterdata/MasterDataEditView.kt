package de.jotbepunkt.blackbook.masterdata

import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.*
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.*

/**
 * Created by bait on 07.07.17.
 */
abstract class MasterDataEditView<T : Entity, V : MasterDataEditView<T, V, C>, C : MasterDataEditController<T, V, C>>(clazz: Class<T>) : HorizontalLayout(), View {

    lateinit var controller: C

    protected val listSelect = ListSelect()
    protected val saveButton = Button("Save")
    protected val addButton = Button("+")
    protected val removeButton = Button("-")

    abstract val formElements: List<Field<out Any>>

    protected val formLayout = FormLayout()

    val fieldGroup = BeanFieldGroup(clazz)

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
                controller.elementSelected(listSelect.value as T)
            }
        }
    }

    override fun attach() {
        initUi()
        super.attach()
    }

    private fun initUi() {
        formElements.forEach { formLayout.addComponent(it) }
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
    }


    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        controller.loadElements()
        isEditorEnabled = false
    }

    @Suppress("UNCHECKED_CAST")
    fun getSelectedElement(): T? = listSelect.value as T?

    fun setItems(elements: List<T>) {
        listSelect.removeAllItems()
        listSelect.addItems(elements)
    }

    fun getEditedElement(): T {
        fieldGroup.commit()
        return fieldGroup.itemDataSource.bean
    }


}

@Suppress("UNCHECKED_CAST")
abstract class MasterDataEditController<T : Entity, V : MasterDataEditView<T, V, C>, C : MasterDataEditController<T, V, C>>
(val constructor: () -> T, val view: V) {

    abstract val repo: MongoRepository<T, String>

    init {
        view.controller = this as C
    }

    fun createElement(): T {
        return constructor.invoke()
    }

    fun addElement() {
        view.fieldGroup.setItemDataSource(createElement())
        view.isEditorEnabled = true
    }

    fun removeElement() {
        val selected = view.getSelectedElement()
        if (selected != null) {
            repo.delete(selected)
        }

        loadElements()
        view.isEditorEnabled = false
    }

    fun loadElements() {
        view.setItems(repo.findAll())
    }

    fun saveElement() {
        val edited = view.getEditedElement()

        if (edited.id == "") {
            repo.insert(edited)
        } else {
            repo.save(edited)
        }
        loadElements()
        view.isEditorEnabled = false
    }

    fun elementSelected(value: T) {
        view.fieldGroup.setItemDataSource(value)
        view.isEditorEnabled = true
    }

}

abstract class Entity {
    @Id var id: String = UUID.randomUUID().toString()


}