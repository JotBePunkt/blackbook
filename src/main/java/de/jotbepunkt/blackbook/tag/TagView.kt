package de.jotbepunkt.blackbook.tag

import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import de.jotbepunkt.blackbook.persistence.Tag
import de.jotbepunkt.blackbook.persistence.TagRepository

/**
 * Created by bait on 06.07.17.
 */

@Component("tags")
class TagView : HorizontalLayout(), View {


    @Autowired lateinit var tagController: TagController
    private val listSelect = ListSelect()
    private val displayName = TextField("Display name")
    private val tag = TextField("Tag")
    private val saveButton = Button("Save")
    private val addButton = Button("+")
    private val removeButton = Button("-")
    private val formLayout = FormLayout(displayName, tag, saveButton)

    val fieldGroup = BeanFieldGroup(Tag::class.java)

    var isEditorEnabled: Boolean = false
        set(value) {
            formLayout.isEnabled = value
            if (!value) {
                fieldGroup.setItemDataSource(Tag())
            }
        }


    init {
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

        fieldGroup.bind(displayName, Tag::displayName.name)
        fieldGroup.bind(tag, Tag::tag.name)

        addButton.addClickListener { tagController.addElement() }
        removeButton.addClickListener { tagController.removeElement() }
        saveButton.addClickListener { tagController.saveElement() }
        listSelect.addValueChangeListener {
            if (listSelect.value != null) {
                tagController.elementSelected(listSelect.value as Tag)
            }
        }
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        tagController.loadElements()
        isEditorEnabled = false
    }

    fun getSelectedElement(): Tag? {
        return listSelect.value as Tag?
    }

    fun setItems(elements: List<Tag>) {
        listSelect.removeAllItems()
        listSelect.addItems(elements)
    }

    fun getEditedElement(): Tag {
        fieldGroup.commit()
        return fieldGroup.itemDataSource.bean
    }
}

@Component
class TagController {

    @Autowired lateinit var view: TagView
    @Autowired lateinit var repo: TagRepository

    fun addElement() {
        view.fieldGroup.setItemDataSource(Tag())
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

    fun elementSelected(value: Tag) {
        view.fieldGroup.setItemDataSource(value)
        view.isEditorEnabled = true
    }
}
