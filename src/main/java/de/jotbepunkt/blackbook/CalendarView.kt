package de.jotbepunkt.blackbook

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.SpringView
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout
import de.jotbepunkt.blackbook.login.LoginController
import org.vaadin.addon.calendar.Calendar
import org.vaadin.addon.calendar.item.EditableCalendarItem
import org.vaadin.addon.calendar.ui.CalendarComponentEvents
import java.time.Duration
import java.time.ZonedDateTime

/**
 * Main view of the calendar
 */
@SpringView(name = "calendar")
class CalendarView(controller: LoginController) : VerticalLayout(), View {

    val calendar = Calendar<EditableCalendarItem>()


    init {

        calendar.setHeight("100%")
        calendar.setWidth("100%")
        calendar.startDate = ZonedDateTime.now()
        calendar.endDate = ZonedDateTime.from(ZonedDateTime.now().plus(Duration.ofDays(30)))

        calendar.addListener {
            print("dies ist ein test")
        }

        calendar.setHandler { it: CalendarComponentEvents.ItemClickEvent ->
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