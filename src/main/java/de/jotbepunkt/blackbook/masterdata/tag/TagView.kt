package de.jotbepunkt.blackbook.masterdata.tag

import com.vaadin.ui.TextField
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Component
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.persistence.Tag

/**
 * Created by bait on 06.07.17.
 */

@Component("tags")
class TagView : MasterDataEditView<Tag, TagView, TagController>(Tag::class.java) {

    private val displayName = TextField("Display name")
    private val tag = TextField("Tag")

    override val formElements = arrayListOf(displayName, tag)


    init {
        fieldGroup.bind(displayName, Tag::displayName.name)
        fieldGroup.bind(tag, Tag::tag.name)
    }

}

@Component
class TagController(@Autowired view: TagView,
                    @Autowired override val repo: MongoRepository<Tag, String>) :
        MasterDataEditController<Tag, TagView, TagController>({ Tag() }, view)