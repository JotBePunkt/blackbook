package de.jotbepunkt.blackbook.login

import com.vaadin.data.Binder
import com.vaadin.event.ShortcutAction
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.*
import de.jotbepunkt.blackbook.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@UIScope
@SpringView(name = "login")
class LoginView(controller: LoginController) : HorizontalLayout(), View {


    val name = TextField("name")
    val password = PasswordField("password")
    val loginButton = Button("login")

    private val form = FormLayout(name, password, loginButton)

    val binder = Binder(LoginModel::class.java)


    init {
        controller.view = this

        binder.bind(name, LoginModel::username.name)
        binder.bind(password, LoginModel::password.name)

        loginButton.addClickListener { controller.doLogin() }
        loginButton.setClickShortcut(ShortcutAction.KeyCode.ENTER)
        loginButton.addStyleName("default")


        val spacer1 = Label()
        val spacer2 = Label()
        addComponent(spacer1)
        addComponent(form)
        form.setWidth("270px")
        addComponent(spacer2)

        setComponentAlignment(form, Alignment.MIDDLE_CENTER)
        setExpandRatio(spacer1, 1f)
        setExpandRatio(spacer2, 1f)

        setSizeFull()
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        binder.bean = LoginModel()
    }
}

@Component
@UIScope
class LoginController {

    @Autowired lateinit var userService: UserService
    lateinit var view: LoginView

    fun doLogin() {
        val loginModel = view.binder.bean

        val user = userService.findByUsername(loginModel.username)
        if (user != null && user.matches(loginModel.password)) {
            UI.getCurrent().navigator.navigateTo("/main/calendar")
        } else {
            Notification.show("Username or password wrong", Notification.Type.ERROR_MESSAGE)
        }
    }
}

class LoginModel {
    var username: String = ""
    var password: String = ""
}

