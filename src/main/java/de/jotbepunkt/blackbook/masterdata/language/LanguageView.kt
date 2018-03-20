package de.jotbepunkt.blackbook.masterdata.language

import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.TextField
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.service.LanguageBusinessObject
import de.jotbepunkt.blackbook.service.LanguageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@SpringView(name = "languages")
class LanguageView : MasterDataEditView<LanguageBusinessObject, LanguageView, LanguageController>(LanguageBusinessObject::class.java) {

    private val name = TextField("Name")
    private val isoCode = TextField("ISO Code")

    override val formElements = bind(
            LanguageBusinessObject::name to name,
            LanguageBusinessObject::isoCode to isoCode)
}

@Component
@ViewScope
class LanguageController @Autowired constructor(override val dataService: LanguageService, view: LanguageView)
    : MasterDataEditController<LanguageBusinessObject, LanguageView, LanguageController>(view) {
}
