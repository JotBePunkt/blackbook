package de.jotbepunkt.blackbook.masterdata.language

import com.vaadin.ui.Field
import com.vaadin.ui.TextField
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.service.LanguageBusinessObject
import de.jotbepunkt.blackbook.service.LanguageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.reflect.KMutableProperty1

@Component("languages")
class LanguageView : MasterDataEditView<LanguageBusinessObject, LanguageView, LanguageController>(LanguageBusinessObject::class.java) {

    private val name = TextField("Name")
    private val isoCode = TextField("ISO Code")

    override val formElements =
            mapOf<KMutableProperty1<LanguageBusinessObject, out Any>, Field<out Any>>(
                    LanguageBusinessObject::name to name, LanguageBusinessObject::isoCode to isoCode)

}

@Component
class LanguageController @Autowired constructor(override val dataService: LanguageService, view: LanguageView)
    : MasterDataEditController<LanguageBusinessObject, LanguageView, LanguageController>(view) {
}
