package de.jotbepunkt.blackbook.login

//import com.vaadin.spring.annotation.SpringView
//import com.vaadin.spring.annotation.UIScope
import com.vaadin.data.fieldgroup.BeanFieldGroup
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.*
import org.springframework.stereotype.Component

//@UIScope
//@SpringView()
@Component("login")
class LoginView(controller: LoginController) : FormLayout(), View {


    val name = TextField("name")
    val password = PasswordField("password")
    val loginButton = Button("login")

    val beanFieldGroup = BeanFieldGroup(LoginModel::class.java)


    init {
        controller.view = this

        beanFieldGroup.bind(name, LoginModel::username.name)
        beanFieldGroup.bind(password, LoginModel::password.name)

        loginButton.addClickListener { controller.doLogin() }

        addComponents(name, password, loginButton)
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        beanFieldGroup.setItemDataSource(LoginModel())
    }
}

@Component
class LoginController {

    lateinit var view: LoginView

    fun doLogin() {
        val loginModel = LoginModel()
        view.beanFieldGroup.itemDataSource.bean

        UI.getCurrent().navigator.navigateTo("/main/calendar")
    }
}

class LoginModel {
    var username: String = ""
    var password: String = ""
}

