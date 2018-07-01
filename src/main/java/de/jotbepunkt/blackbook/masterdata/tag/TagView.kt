package de.jotbepunkt.blackbook.masterdata.tag

import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.TextField
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.service.TagBo
import de.jotbepunkt.blackbook.service.TagService
import de.jotbepunkt.blackbook.vaadin.to
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by bait on 06.07.17.
 */

@SpringView(name = "tags")
class TagView
@Autowired constructor(controller: TagController) : MasterDataEditView<TagBo, TagView, TagController>(TagBo::class.java, controller) {

    private val displayName = TextField("Display name")
    private val tag = TextField("Tag")

    override val formElements = bind(
            TagBo::displayName to displayName,
            TagBo::tag to tag)
}

@Component
@ViewScope
class TagController @Autowired constructor(override val dataService: TagService) :
        MasterDataEditController<TagBo, TagView, TagController>()