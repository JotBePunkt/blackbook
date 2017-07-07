package de.jotbepunkt.blackbook

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.navigator.ViewProvider
import com.vaadin.ui.Alignment
import com.vaadin.ui.Button
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.VerticalLayout
import org.springframework.stereotype.Component
import de.jotbepunkt.blackbook.navigation.NestedView
import de.jotbepunkt.blackbook.navigation.navigateTo

/**
 * Created by bait on 21.06.17.
 */
@Component("main")
class MainView(val viewProvider: ViewProvider) : NestedView, VerticalLayout() {

    override var parent: NestedView? = null


    override fun showView(view: View?) {
        contentArea.removeAllComponents()
        contentArea.addComponent(view as com.vaadin.ui.Component)
    }

    val calendarButton = Button("Calendar")
    val eventTypes = Button("Event Types")
    val tagsButton = Button("Tags")
    val userButton = Button("User")
    val logoutButton = Button("Logout")
    val navigationBar = HorizontalLayout(calendarButton, eventTypes, tagsButton, userButton, logoutButton)
    val contentArea = VerticalLayout()


    init {
        with(navigationBar) {
            setWidth("100%")
            setComponentAlignment(logoutButton, Alignment.MIDDLE_RIGHT)
            setExpandRatio(logoutButton, 1f)
        }

        calendarButton.addClickListener { navigateTo("calendar") }
        eventTypes.addClickListener { navigateTo("eventTypes") }
        tagsButton.addClickListener { navigateTo("tags") }
        userButton.addClickListener { navigateTo("users") }
        logoutButton.addClickListener { navigateTo("logout") }

        addComponents(navigationBar, contentArea)
        setExpandRatio(contentArea, 1f)

        calendarButton.addClickListener { }
    }


    override fun enter(event: ViewChangeListener.ViewChangeEvent) {
        // currenly nothign maybe later remove the buttons that the user
    }
}