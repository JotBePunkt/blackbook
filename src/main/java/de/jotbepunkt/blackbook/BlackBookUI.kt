package de.jotbepunkt.blackbook

import com.vaadin.annotations.PreserveOnRefresh
import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewProvider
import com.vaadin.server.VaadinRequest
import com.vaadin.server.VaadinServlet
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.UI
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import de.jotbepunkt.blackbook.navigation.NestedNavigator

@Component
@Title("Quälgeist - Schwarzes Buch")
@PreserveOnRefresh
@Theme("valo")
class BlackBookUI : UI() {

    override fun init(vaadinRequest: VaadinRequest) {


        val layout = HorizontalLayout()
        layout.setHeight("100%")
        layout.setWidth("100%")
        content = layout


        val appContext = (VaadinServlet.getCurrent() as Servlet).appContext
        val viewProvider = appContext.getBean(SpringViewProvider::class.java)
        navigator = NestedNavigator(this, Navigator.ComponentContainerViewDisplay(layout))
        navigator.addProvider(viewProvider)

        if (session.getAttribute("user") == null) {
            navigator.navigateTo("login")
        } else {
            navigator.navigateTo("")
        }
    }
}

@Component
class SpringViewProvider : ViewProvider, ApplicationContextAware {
    lateinit var appContext: ApplicationContext
        private set

    override fun setApplicationContext(context: ApplicationContext?) {
        if (context != null) {
            appContext = context
        }
    }

    override fun getView(viewName: String?): View {
        return appContext.getBean(viewName, View::class.java)
    }

    override fun getViewName(viewAndParameter: String?): String {
        if (viewAndParameter == null) {
            throw IllegalArgumentException("null now allowed")
        } else if (viewAndParameter.indexOf('?') == -1) {
            return viewAndParameter
        } else {
            return viewAndParameter.substring(0, viewAndParameter.indexOf('?'))
        }
    }

}
