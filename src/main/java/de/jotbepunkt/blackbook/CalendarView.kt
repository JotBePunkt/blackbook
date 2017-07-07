package de.jotbepunkt.blackbook

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.ui.Calendar
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout
import com.vaadin.ui.components.calendar.CalendarComponentEvents
import org.springframework.stereotype.Component
import de.jotbepunkt.blackbook.login.LoginController
import java.time.Duration
import java.time.Instant
import java.util.*

/**
 * Main view of the calendar
 */
//@UIScope
//@SpringView(name = "")
@Component("calendar")
class CalendarView(controller: LoginController) : VerticalLayout(), View {

    val calendar = Calendar()


    init {

        calendar.setHeight("100%")
        calendar.setWidth("100%")
        calendar.startDate = Date.from(Instant.now())
        calendar.endDate = Date.from(Instant.now().plus(Duration.ofDays(30)))

        calendar.addListener {
            print("dies ist ein test")
        }

        calendar.setHandler { it: CalendarComponentEvents.EventClick ->
            Notification.show("event clicked " + it)
        }

        calendar.setHandler { it: CalendarComponentEvents.DateClickEvent ->
            Notification.show("date clicked " + it)
        }

        calendar.setHandler { it: CalendarComponentEvents.WeekClick ->
            Notification.show("week clicked " + it)
        }

        calendar.setHandler { it: CalendarComponentEvents.RangeSelectEvent ->
            Notification.show("range selected " + it)
        }

        addComponent(calendar)
    }


    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {

    }

}