package de.jotbepunkt.blackbook.calendar

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.Button
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.vaadin.addon.calendar.Calendar
import org.vaadin.addon.calendar.item.EditableCalendarItem
import org.vaadin.addon.calendar.ui.CalendarComponentEvents
import java.time.ZonedDateTime

/**
 * Main view of the calendar
 */
@SpringView(name = "calendar")
class CalendarView
@Autowired constructor(val eventView: EventView, val controller: CalendarController) : VerticalLayout(), View {

    private val calendar = Calendar<EditableCalendarItem>()

    private val monthButton = Button("Month")
    private val weekButton = Button("week")
    private val backButton = Button("<<")
    private val forwardButton = Button(">>")

    init {
        controller.view = this
        setMargin(false)

        val buttonLayout = HorizontalLayout(backButton, monthButton, weekButton, forwardButton)
        addComponent(buttonLayout)

        calendar.setHeight("100%")
        calendar.setWidth("100%")
        calendar.withMonth(ZonedDateTime.now().month)

        calendar.addListener {
            print("dies ist ein test")
        }

        calendar.setHandler { it: CalendarComponentEvents.ItemClickEvent ->
            Notification.show("event clicked $it")
        }

        calendar.setHandler { it: CalendarComponentEvents.DateClickEvent ->
            if (calendar.isMonthlyMode) {
                calendar.withWeek(it.date)
            } else {
                Notification.show("Was erwartest du ws passiert wenn du hier klickst?")
            }
        }

        calendar.setHandler { it: CalendarComponentEvents.WeekClick ->
            calendar.withWeekInYear(it.week)
        }

        calendar.setHandler { it: CalendarComponentEvents.RangeSelectEvent ->
            if (calendar.isMonthlyMode) {
                if (it.start.dayOfMonth != it.end.dayOfMonth) {

                    Notification.show("Now we should create a multi day event")
                } else {
                    calendar.withWeek(it.start)
                }
            } else if (calendar.isWeeklyMode) {

                controller.showNewEvent(it.start, it.end)
            }
        }
        addComponent(calendar)

        monthButton.addClickListener { calendar.withMonth(calendar.startDate.month) }
        backButton.addClickListener {
            if (calendar.isMonthlyMode) {
                val newStartDate = calendar.startDate.minusMonths(1)
                calendar.withMonthInYear(newStartDate.month, newStartDate.year)
            } else {
                calendar.withWeek(calendar.startDate.minusWeeks(1))
            }
        }
        forwardButton.addClickListener {
            if (calendar.isMonthlyMode) {
                val newStartDate = calendar.startDate.plusMonths(1)
                calendar.withMonthInYear(newStartDate.month, newStartDate.year)
            } else {
                calendar.withWeek(calendar.startDate.plusWeeks(1))
            }
        }
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
    }

}

@ViewScope
@Component
class CalendarController
@Autowired constructor(private val eventController: EventController) {
    lateinit var view: CalendarView

    fun showNewEvent(start: ZonedDateTime, end: ZonedDateTime) {
        eventController.showNewEvent(start, end)
    }

}