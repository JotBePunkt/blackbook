package de.jotbepunkt.blackbook.masterdata.user

import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.PasswordField
import com.vaadin.ui.TextField
import de.jotbepunkt.blackbook.masterdata.MasterDataEditController
import de.jotbepunkt.blackbook.masterdata.MasterDataEditView
import de.jotbepunkt.blackbook.service.BusinessService
import de.jotbepunkt.blackbook.service.UserBo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by bait on 30.09.17.
 */
@SpringView(name = "users")
class UserView : MasterDataEditView<UserBo, UserView, UserController>(UserBo::class.java) {

    val username = TextField("Login")
    val name = TextField("Name")
    val password = PasswordField("Password")

    override val formElements
        get() = bind(
                UserBo::username to username,
                UserBo::name to name,
                UserBo::password to password
        )
}

@Component
@ViewScope
class UserController(
        @Autowired view: UserView,
        @Autowired override val dataService: BusinessService<*, UserBo>) :
        MasterDataEditController<UserBo, UserView, UserController>(view) {
}
