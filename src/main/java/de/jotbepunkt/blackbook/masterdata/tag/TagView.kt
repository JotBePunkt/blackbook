package de.jotbepunkt.blackbook.masterdata.tag

import com.vaadin.ui.Field
import com.vaadin.ui.TextField
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.persistence.Tag
import de.jotbepunkt.blackbook.service.TagBo
import de.jotbepunkt.blackbook.service.TagService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.reflect.KMutableProperty1

/**
 * Created by bait on 06.07.17.
 */

@Component("tags")
class TagView : MasterDataEditView<TagBo, TagView, TagController>(TagBo::class.java) {

    private val displayName = TextField("Display name")
    private val tag = TextField("Tag")

    override val formElements = mapOf<KMutableProperty1<TagBo, out Any>, Field<out Any>>(
            TagBo::displayName to displayName, TagBo::tag to tag)


    init {
        fieldGroup.bind(displayName, Tag::displayName.name)
        fieldGroup.bind(tag, Tag::tag.name)
    }
}

@Component
class TagController @Autowired constructor(view: TagView, override val dataService: TagService) :
        MasterDataEditController<TagBo, TagView, TagController>(view)