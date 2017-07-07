package de.jotbepunkt.blackbook.navigation

import com.vaadin.navigator.*
import com.vaadin.shared.util.SharedUtil
import com.vaadin.ui.Component
import com.vaadin.ui.HorizontalLayout
import com.vaadin.ui.UI

/**
 * Interface for a nested view
 */

interface NestedView : View, ViewDisplay {
    var parent: NestedView?
}

fun NestedView.navigateTo(path: String) {
    (UI.getCurrent().navigator as NestedNavigator).navigateTo(this, path)
}

class RootView(val viewDisplay: ViewDisplay) : NestedView {

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        // nothing happens, this is the root view
    }

    override fun showView(view: View?) {
        if (view != null) {
            viewDisplay.showView(view)
        } else {
            viewDisplay.showView(Navigator.EmptyView())
        }

    }

    override var parent: NestedView?
        get() = null // This is the root, so we dont have a member here
        set(value) {}

}

class NestedNavigator(ui: UI?, display: ViewDisplay) : Navigator(ui, display) {

    val currentNaviationStates: MutableList<String> = ArrayList()
    val currentPath: MutableList<NestedView> = ArrayList()


    init {
        currentPath += RootView(display)
        currentNaviationStates += ""
    }

    @Deprecated(message = "this does not contain any information about nesting, therefore this method is not supported",
            level = DeprecationLevel.HIDDEN,
            replaceWith = ReplaceWith("navigateTo"))
    override fun navigateTo(view: View, viewName: String, parameters: String) {
        throw IllegalStateException("this is not supposed to be used. Please use the other navigateTo functions")
    }

    override fun navigateTo(navigationState: String) {
        navigateTo(currentPath[0], navigationState)
    }

    fun navigateTo(view: NestedView, path: String) {
        val splittedPath = path.split('/')

        if (path.startsWith('/')) {
            // remove the first element a it is an empty string
            navigateToAbsolute(splittedPath.subList(1, splittedPath.size))
        } else {
            navigateToRelative(view, splittedPath)
        }
    }

    fun navigateTo(view: NestedView, path: List<String>) {
        navigateToRelative(view, path)
    }

    private fun navigateToRelative(view: NestedView, path: List<String>) {
        val parent = view.parent
        if (path[0] == ".." && parent != null) {
            navigateToRelative(parent, path.subList(1, path.size))
        } else if (path[0] == ".." && parent != null) {
            navigateToRelative(view, path.subList(1, path.size))
        } else {
            val pathToView = getPathToView(view)
            val absolutePath = pathToView + path
            navigateToAbsolute(absolutePath)
        }

    }

    private fun getPathToView(view: NestedView): List<String> {

        val toSearch = currentPath.find { it == view || (it is NestedViewWrapper && it.view == view) }

        if (toSearch == null) {
            throw IllegalArgumentException("did not find the view in the current path")
        } else {
            val index = currentPath.indexOf(toSearch)
            return currentNaviationStates.subList(0, index)
        }
    }

    private fun navigateToAbsolute(splittedPath: List<String>) {

        splittedPath.forEachIndexed { index, navigationState ->
            val (longestViewName: String, viewWithLongestName: NestedView) = getView(navigationState)
            val (parameters: String, usedNavigationState: String) = getUsedNavigationStateAndViewName(navigationState, longestViewName)
            navigateToView(viewWithLongestName, usedNavigationState, longestViewName, parameters, index)
        }
    }

    private fun getUsedNavigationStateAndViewName(navigationState: String, longestViewName: String): Pair<String, String> {
        val parameters: String
        val usedNavigationState: String
        if (navigationState.length > longestViewName.length + 1) {
            usedNavigationState = navigationState
            parameters = navigationState.substring(longestViewName.length + 1)
        } else if (navigationState.endsWith("/")) {
            usedNavigationState = navigationState.substring(0,
                    navigationState.length - 1)
            parameters = ""
        } else {
            usedNavigationState = navigationState
            parameters = ""
        }
        return Pair(parameters, usedNavigationState)
    }

    private fun navigateToView(viewWithLongestName: NestedView,
                               usedNavigationState: String,
                               longestViewName: String,
                               parameters: String,
                               index: Int) {
        if (!SharedUtil.equals(getCurrentView(index), viewWithLongestName)
                || !SharedUtil.equals(currentNaviationStates[index], usedNavigationState)) {

            removedUpperPathElements(index)
            navigateTo(viewWithLongestName, longestViewName, parameters, index)
        } else {
            updateNavigationState(ViewChangeListener.ViewChangeEvent(this, getCurrentView(index),
                    viewWithLongestName, longestViewName, parameters), index)
        }
    }

    private fun removedUpperPathElements(index: Int) {
        currentPath.removeAllIndexed { i, _ -> i > index }
        currentNaviationStates.removeAllIndexed { i, _ -> i > index }
    }

    fun <E> MutableList<E>.removeAllIndexed(filter: (Int, E) -> Boolean) {
        val toBeRemoved = this.filterIndexed(filter)
        this.removeAll(toBeRemoved)

    }


    private fun getView(navigationState: String): Pair<String, NestedView> {
        var (longestViewName: String?, viewWithLongestName: NestedView?) = getRegularView(navigationState)

        if (viewWithLongestName == null && errorProvider != null) {
            val pair = getErrorView(navigationState)
            longestViewName = pair.first
            viewWithLongestName = pair.second
        }

        if (viewWithLongestName == null) {
            throw IllegalArgumentException(
                    "Trying to navigate to an unknown state '" + navigationState
                            + "' and an error view provider not present")
        }

        return Pair(longestViewName!!, viewWithLongestName)
    }

    private fun getErrorView(navigationState: String): Pair<String?, NestedView?> {
        val longestViewName = errorProvider?.getViewName(navigationState)
        val viewWithLongestName = errorProvider?.getView(longestViewName) as NestedView
        return Pair(longestViewName, viewWithLongestName)
    }

    private fun getRegularView(navigationState: String): Pair<String?, NestedView?> {
        val longestViewNameProvider = getViewProvider(navigationState)
        val longestViewName: String? = longestViewNameProvider.getViewName(navigationState)
        var viewWithLongestName: NestedView? = null

        if (longestViewName != null) {
            val view = longestViewNameProvider.getView(longestViewName)
            if (view is NestedView) {
                viewWithLongestName = view
            } else {
                viewWithLongestName = NestedViewWrapper(view)
            }
        }
        return Pair(longestViewName, viewWithLongestName)
    }


    private fun getViewProvider(navigationState: String): ViewProvider {
        val function = javaClass.superclass.getDeclaredMethod("getViewProvider", String::class.java)
        function.isAccessible = true
        return function.invoke(this, navigationState) as ViewProvider
    }

    private fun navigateTo(view: NestedView, viewName: String, parameters: String, index: Int) {

        val event = ViewChangeListener.ViewChangeEvent(this, getCurrentView(index + 1), view,
                viewName, parameters)
        val navigationAllowed = beforeViewChange(event)
        if (!navigationAllowed) {
            // #10901. Revert URL to previous state if back-button navigation
            // was canceled
            revertNavigation()
            return
        }

        updateNavigationState(event, index)

        getCurrentView(index)!!.showView(view)

        switchView(event)

        view.enter(event)

        fireAfterViewChange(event)

        currentPath += view
    }

    private val errorProvider: ViewProvider?
        get() {
            val errorProviderField = javaClass.getField("errorProvider")
            errorProviderField.isAccessible = true
            return errorProviderField.get(this) as ViewProvider
        }

    @Deprecated(message = "this does not contain any information about nesting, therefore this method is not supported",
            level = DeprecationLevel.ERROR,
            replaceWith = ReplaceWith("getCurrentView(Int)"))
    override fun getCurrentView(): View {
        return super.getCurrentView()
    }

    fun getCurrentView(depth: Int): NestedView? {
        if (depth < currentPath.size) {
            return currentPath[depth]
        } else {
            return null
        }
    }

    fun updateNavigationState(event: ViewChangeListener.ViewChangeEvent, index: Int) {
        val viewName = event.viewName
        val parameters = event.parameters
        if (null != viewName && stateManager != null) {
            var navigationState: String = viewName
            if (!parameters.isEmpty()) {
                navigationState += "?" + parameters
            }
            if (navigationState != stateManager.state) {
                stateManager.state = navigationState
            }

            if (currentNaviationStates.size > index) {
                currentNaviationStates[index] = navigationState
            } else {
                currentNaviationStates += navigationState
            }
        }
    }
}

class NestedViewWrapper(val view: View) : NestedView, HorizontalLayout() {

    override var parent: NestedView? = null

    init {
        addComponent(view as Component)
        addStyleName("wrapper-" + view.javaClass.simpleName)
    }

    override fun enter(event: ViewChangeListener.ViewChangeEvent?) {
        view.enter(event)
    }

    override fun showView(view: View?) {
        throw IllegalStateException("This is not a nested view")
    }
}
