package de.jotbepunkt.blackbook

import com.vaadin.annotations.PreserveOnRefresh
import com.vaadin.annotations.Theme
import com.vaadin.annotations.Title
import com.vaadin.annotations.Widgetset
import com.vaadin.navigator.Navigator
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewProvider
import com.vaadin.server.VaadinRequest
import com.vaadin.spring.annotation.SpringUI
import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.SpringViewDisplay
import com.vaadin.spring.annotation.UIScope
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.UI
import com.vaadin.ui.VerticalLayout
import de.jotbepunkt.blackbook.navigation.NestedNavigator
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
@Title("Schwarzes Buch")
@PreserveOnRefresh
@Widgetset("de.jotbepunkt.blackbook.AppWidgetset") // note: this is casesensitive
@Theme("valo")
@SpringUI
@UIScope
@SpringViewDisplay
class BlackBookUI : UI(), ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext
    override fun setApplicationContext(p0: ApplicationContext?) {
        applicationContext = p0!!
    }

    override fun init(vaadinRequest: VaadinRequest) {
        val layout = HorizontalLayout()
        layout.setHeight("100%")
        layout.setWidth("100%")
        content = layout

        val viewProvider = applicationContext.getBean(ViewProvider::class.java)
        navigator = NestedNavigator(this, Navigator.ComponentContainerViewDisplay(layout))
        navigator.addProvider(viewProvider)

        if (session.getAttribute("user") == null) {
            navigator.navigateTo("login")
        } else {
            navigator.navigateTo("")
        }
    }
}

@SpringView
class NullView : View, VerticalLayout()
