package portaria.project.navigation

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

class AppNavigator internal constructor(
    private val backStack: SnapshotStateList<AppRoute>
) {
    fun navigate(route: AppRoute) {
        backStack.add(route)
    }

    fun navigateReplaceRoot(route: AppRoute) {
        backStack.clear()
        backStack.add(route)
    }

    fun popBackStack(): Boolean {
        if (backStack.size <= 1) return false
        backStack.removeAt(backStack.lastIndex)
        return true
    }
}

fun createAppNavigator(backStack: SnapshotStateList<AppRoute>) = AppNavigator(backStack)

fun initialAppBackStack(): SnapshotStateList<AppRoute> = mutableStateListOf(AppRoute.Login)
