package de.jotbepunkt.blackbook.calendar

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener
import com.vaadin.spring.annotation.SpringView
import com.vaadin.spring.annotation.ViewScope
import com.vaadin.ui.Button
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.Notification
import com.vaadin.ui.VerticalLayout
import de.jotbepunkt.blackbook.calendar.CalendarView.EditableCalendarEvent
import de.jotbepunkt.blackbook.navigation.navigateTo
import de.jotbepunkt.blackbook.service.EventBo
import de.jotbepunkt.blackbook.service.SingleEventBo
import de.jotbepunkt.blackbook.service.SingleEventService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.vaadin.addon.calendar.Calendar
import org.vaadin.addon.calendar.item.CalendarItemProvider
import org.vaadin.addon.calendar.item.EditableCalendarItem
import org.vaadin.addon.calendar.ui.CalendarComponentEvents
import java.time.*

/**
 * Main view of the calendar
 */
@SpringView(name = CalendarView.VIEWNAME)
class CalendarView
@Autowired constructor(val eventView: EventView, val controller: CalendarController) : VerticalLayout(), View {

    companion object {
        const val VIEWNAME = "calendar"
    }

    private val calendar = Calendar<EditableCalendarItem>().apply {
        setHeight("100%")
        setWidth("100%")

        dataProvider = CalendarItemProvider { startDate, endDate ->
            controller.getEvents(
                    LocalDate.from(startDate),
                    LocalDate.from(endDate)
            ).map(::toCalenderItem)
        }

        setHandler { it: CalendarComponentEvents.ItemClickEvent ->
            controller.editEvent(it.calendarItem as EditableCalendarEvent)
        }

        setHandler { it: CalendarComponentEvents.DateClickEvent ->
            if (isMonthlyMode) {
                navigateTo(it.date.toInstant(), CalendarViewMode.WEEK)
            } else {
                Notification.show("Was erwartest du ws passiert wenn du hier klickst?")
            }
        }

        setHandler { it: CalendarComponentEvents.WeekClick ->
            navigateTo(
                    LocalDate.ofYearDay(it.year, it.week * 7)
                            .atStartOfDay()
                            .toInstant(ZoneOffset.UTC),
                    CalendarViewMode.WEEK)
        }

        setHandler { it: CalendarComponentEvents.RangeSelectEvent ->
            if (isMonthlyMode) {
                if (it.start.dayOfMonth != it.end.dayOfMonth) {
                    Notification.show("Now we should create a multi day event")
                } else {
                    navigateTo(it.start.toInstant(), CalendarViewMode.WEEK)
                }
            } else if (isWeeklyMode) {
                controller.showNewEvent(it.start, it.end)
            }
        }
    }

    interface EditableCalendarEvent : EditableCalendarItem {
        val eventBo: EventBo<*>
    }

    private fun toCalenderItem(eventBo: EventBo<*>) =
            object : EditableCalendarEvent {
                override val eventBo: EventBo<*>
                    get() = eventBo

                override fun getNotifier(): EditableCalendarItem.ItemChangeNotifier? = null

                override fun getEnd() = ZonedDateTime.of(eventBo.end, ZoneId.systemDefault())

                override fun setCaption(caption: String?) =
                        controller.setEventCaption(eventBo, caption)

                override fun getCaption() = eventBo.title

                override fun setEnd(end: ZonedDateTime?) {
                    controller.setEventEnd(eventBo, end)
                }

                override fun getStart(): ZonedDateTime = ZonedDateTime.of(eventBo.start, ZoneId.systemDefault())

                override fun setDescription(description: String?) =
                        controller.setEventDescription(eventBo, description)

                override fun getDescription() = eventBo.comment

                override fun getStyleName(): String? {
                    return null
                }

                override fun setStart(start: ZonedDateTime?) {
                    controller.setEventStart(eventBo, start)
                }

                override fun setAllDay(isAllDay: Boolean) {
                    controller.setAllDay(eventBo, isAllDay)
                }

                override fun setStyleName(styleName: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            }

    private val monthButton = Button("Month")
    private val weekButton = Button("week")
    private val backButton = Button("<<")
    private val forwardButton = Button(">>")

    init {
        controller.view = this
        setMargin(false)

        val buttonLayout = HorizontalLayout(backButton, monthButton, weekButton, forwardButton)
        addComponent(buttonLayout)
        calendar.withMonth(ZonedDateTime.now().month)
        addComponent(calendar)

        monthButton.addClickListener { calendar.withMonth(calendar.startDate.month) }
        backButton.addClickListener {
            if (calendar.isMonthlyMode) {
                navigateTo(
                        calendar.startDate.minusMonths(1).toInstant(),
                        CalendarViewMode.MONTH)
            } else {
                navigateTo(
                        calendar.startDate.minusWeeks(1).toInstant(),
                        CalendarViewMode.WEEK)
            }
        }
        forwardButton.addClickListener {
            if (calendar.isMonthlyMode) {
                val newStartDate = calendar.startDate.plusMonths(1)
                navigateTo(newStartDate.toInstant(), CalendarViewMode.MONTH)
            } else {
                navigateTo(
                        calendar.startDate.plusWeeks(1).toInstant(),
                        CalendarViewMode.WEEK)
            }
        }
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent) {

        if (event.parameterMap["date"] == null)
            navigateToDefault()
        else {
            try {
                val date = event.parameterMap["date"]!!.toLong()

                val viewMode = event.parameterMap["view"]
                when (viewMode) {
                    null -> navigateToDefault()
                    CalendarViewMode.WEEK.toString() -> gotoWeek(date)
                    CalendarViewMode.MONTH.toString() -> gotoMonth(date)
                }
            } catch (e: NumberFormatException) {
                navigateToDefault()
            }
        }
    }

    private fun gotoWeek(date: Long) {
        val parsedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())
        calendar.withWeek(parsedDate)
    }

    private fun gotoMonth(date: Long) {
        val parsedDate = LocalDateTime.ofEpochSecond(date / 1000, 0, ZoneOffset.UTC)
        calendar.withMonthInYear(parsedDate.month, parsedDate.year)
    }

    private fun navigateToDefault() =
            navigateTo(Instant.now(), CalendarViewMode.MONTH)

    private fun navigateTo(date: Instant, viewModeType: CalendarViewMode) {
        navigateTo("../$VIEWNAME&view=$viewModeType&date=${date.toEpochMilli()}")
    }

    enum class CalendarViewMode {
        MONTH, WEEK
    }

}

@ViewScope
@Component
class CalendarController
@Autowired constructor(
        private val eventController: EventController,
        private val singleEventService: SingleEventService) {
    lateinit var view: CalendarView

    fun showNewEvent(start: ZonedDateTime, end: ZonedDateTime) {
        eventController.showNewEvent(start, end)
    }

    fun editEvent(calendarItem: EditableCalendarEvent) {
        eventController.editEvent(calendarItem.eventBo.id)
    }

    /**
     * Loads the event between the dates. Actually it looks also for events one
     * day before the earliest day to make sure we don't miss an event over the night
     */
    fun getEvents(from: LocalDate, to: LocalDate): List<EventBo<*>> =
            singleEventService.findBetween(from.minusDays(1), to)

    fun setEventCaption(eventBo: EventBo<*>, caption: String?) {
        TODO("not yet implemented")
    }

    fun setEventEnd(eventBo: EventBo<*>, end: ZonedDateTime?) {
        eventBo.end = LocalDateTime.from(end)
        singleEventService.save(eventBo as SingleEventBo)
    }

    fun setEventDescription(eventBo: EventBo<*>, description: String?) {
        TODO("not yet implemented")
    }

    fun setEventStart(eventBo: EventBo<*>, start: ZonedDateTime?) {
        eventBo.start = LocalDateTime.from(start)
        singleEventService.save(eventBo as SingleEventBo)
    }

    fun setAllDay(eventBo: EventBo<*>, allDay: Boolean) {
        TODO("not yet implemented")
    }


}