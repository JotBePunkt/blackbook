package de.jotbepunkt.blackbook.navigation

import com.vaadin.navigator.*
import com.vaadin.shared.util.SharedUtil
import com.vaadin.ui.Component
import com.vaadin.ui.UI

/**
 * Interface for a nested view
 */


fun View.navigateTo(path: String) {
    (UI.getCurrent().navigator as NestedNavigator).navigateTo(this, path)
}

class NestedNavigator(ui: UI?, display: ViewDisplay) : Navigator(ui, display) {

    private val currentNavigationStates: MutableList<String> = arrayListOf("")
    private val currentPath: MutableList<View> = arrayListOf(RootView(display))

    @Deprecated(message = "this does not contain any information about nesting, therefore this method is not supported",
            level = DeprecationLevel.HIDDEN,
            replaceWith = ReplaceWith("navigateTo"))
    override fun navigateTo(view: View, viewName: String, parameters: String) {
        throw IllegalStateException("this is not supposed to be used. Please use the other navigateTo functions")
    }

    override fun navigateTo(navigationState: String) {
        navigateTo(null, navigationState)
    }

    fun navigateTo(relativeTo: View?, path: String) {
        val splittedPath = path.split('/')

        if (path.startsWith('/')) {
            // remove the first element a it is an empty string
            navigateToAbsolute(splittedPath.subList(1, splittedPath.size))
        } else {
            navigateToRelative(relativeTo, splittedPath)
        }
    }

    fun navigateTo(view: View, path: List<String>) {
        navigateToRelative(view, path)
    }

    private fun navigateToRelative(relativeToView: View?, path: List<String>) {
        val parent = relativeToView?.parent
        if (path[0] == ".." && parent != null) {
            navigateToRelative(parent, path.subList(1, path.size))
        } else if (path[0] == ".." && parent == null) {
            navigateToRelative(relativeToView, path.subList(1, path.size))
        } else {
            val pathToView = getPathToView(relativeToView)
            val absolutePath = pathToView + path
            navigateToAbsolute(absolutePath)
        }

    }

    private fun getPathToView(view: View?): List<String> {

        if (view == null) {
            return emptyList()
        }

        val index = currentPath.indexOf(view)
        return currentNavigationStates.subList(0, index)
    }

    private fun navigateToAbsolute(splittedPath: List<String>) {

        splittedPath.forEachIndexed { index, navigationState ->
            val (longestViewName: String, viewWithLongestName: View) = getView(navigationState)
            val (parameters: String, usedNavigationState: String) = getUsedNavigationStateAndViewName(navigationState, longestViewName)
            navigateToView(viewWithLongestName, usedNavigationState, longestViewName, parameters, index)
        }
    }

    private fun getUsedNavigationStateAndViewName(navigationState: String, longestViewName: String): Pair<String, String> {
        val parameters: String
        val usedNavigationState: String
        when {
            navigationState.length > longestViewName.length + 1 -> {
                usedNavigationState = navigationState
                parameters = navigationState.substring(longestViewName.length + 1)
            }
            navigationState.endsWith("/") -> {
                usedNavigationState = navigationState.substring(0,
                        navigationState.length - 1)
                parameters = ""
            }
            else -> {
                usedNavigationState = navigationState
                parameters = ""
            }
        }
        return Pair(parameters, usedNavigationState)
    }

    private fun navigateToView(viewWithLongestName: View,
                               usedNavigationState: String,
                               longestViewName: String,
                               parameters: String,
                               index: Int) {
        if (!SharedUtil.equals(getCurrentView(index), viewWithLongestName)
                || !SharedUtil.equals(currentNavigationStates[index], usedNavigationState)) {

            removedUpperPathElements(index)
            navigateTo(viewWithLongestName, longestViewName, parameters, index)
        } else {
            updateNavigationState(ViewChangeListener.ViewChangeEvent(this, getCurrentView(index),
                    viewWithLongestName, longestViewName, parameters), index)
        }
    }

    private fun removedUpperPathElements(index: Int) {
        currentPath.removeAllIndexed { i, _ -> i > index }
        currentNavigationStates.removeAllIndexed { i, _ -> i > index }
    }

    private fun <E> MutableList<E>.removeAllIndexed(filter: (Int, E) -> Boolean) {
        val toBeRemoved = this.filterIndexed(filter)
        this.removeAll(toBeRemoved)

    }

    private fun getView(navigationState: String): Pair<String, View> {
        var (longestViewName: String?, viewWithLongestName: View?) = getRegularView(navigationState)

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

    private fun getErrorView(navigationState: String): Pair<String?, View?> {
        val longestViewName = errorProvider?.getViewName(navigationState)
        val viewWithLongestName = errorProvider?.getView(longestViewName) as View
        return Pair(longestViewName, viewWithLongestName)
    }

    private fun getRegularView(navigationState: String): Pair<String?, View?> {
        // traditionally in a non-nested world, the slash is used to differentiate viewname/parameter
        // instead of the ampersand as we do it here
        // we don't want to rewrite the logic of getViewPrider, so we just convert the naviagation state
        // to the traditional format
        val traditionalNavigationState = navigationState.replace("&", "/")
        val longestViewNameProvider = getViewProvider(traditionalNavigationState)
                ?: throw IllegalStateException("View '$navigationState' does not exist")
        val longestViewName: String? = longestViewNameProvider.getViewName(traditionalNavigationState)
        var viewWithLongestName: View? = null

        if (longestViewName != null) {
            val view = longestViewNameProvider.getView(longestViewName)
            viewWithLongestName = view
        }
        return Pair(longestViewName, viewWithLongestName)
    }

    private fun navigateTo(view: View, viewName: String, parameters: String, index: Int) {

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
        currentPath += view
        view.enter(event)
        fireAfterViewChange(event)
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

    fun getCurrentView(depth: Int): View? =
            if (depth < currentPath.size)
                currentPath[depth]
            else
                null


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

            if (currentNavigationStates.size > index) {
                currentNavigationStates[index] = navigationState
            } else {
                currentNavigationStates += navigationState
            }
        }
    }
}

private class RootView(val rootDisplay: ViewDisplay) : View

private fun View.showView(view: View) {
    when {
        this is RootView -> rootDisplay.showView(view)
        this is ViewDisplay -> (this as ViewDisplay).showView(view)
        this.parent != null -> this.parent!!.showView(view)
        else -> throw IllegalStateException("this was not expected")
    }
}

private val View.parent: View?
    get() = (this as Component).parentView

private val Component.parentView: View?
    get() {
        return when {
            parent is View -> parent as View
            parent != null -> parent.parentView
            else -> null
        }
    }
