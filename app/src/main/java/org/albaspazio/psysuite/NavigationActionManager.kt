package org.albaspazio.psysuite

import android.app.Activity
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import org.albaspazio.core.fragments.BaseFragment

/**
 * Centralized navigation action manager that resolves navigation actions
 * based on current fragment and target destination.
 * 
 * Eliminates code duplication between ResultsManager and MainActivity
 * by providing a single source of truth for fragment-to-action mappings.
 */
object NavigationActionManager {
    
    private const val TAG = "NavigationActionManager"
    
    /**
     * Supported navigation destinations
     */
    enum class NavigationDestination {
        TEST_FRAGMENT,
        RESULTS_MANAGER_FRAGMENT
    }
    
    /**
     * Navigation mappings from fragment to destination actions
     */
    private val navigationMappings = mapOf(
        NavigationDestination.TEST_FRAGMENT to mapOf(
            "MainFragment"              to R.id.action_mainFragment_to_testFragment,
            "TemporalTestsFragment"     to R.id.action_temporalTestsFragment_to_testFragment,
            "IllusionTestsFragment"     to R.id.action_illusionTestsFragment_to_testFragment,
            "PredictionTestsFragment"   to R.id.action_predictionTestsFragment_to_testFragment,
            "VariousTestsFragment"      to R.id.action_variousTestsFragment_to_testFragment,
            "BindingsFragment"          to R.id.action_bindingsFragment_to_testFragment
        ),
        NavigationDestination.RESULTS_MANAGER_FRAGMENT to mapOf(
            "MainFragment"              to R.id.action_mainFragment_to_resultsManagerFragment,
            "TemporalTestsFragment"     to R.id.action_temporalTestsFragment_to_resultsManagerFragment,
            "IllusionTestsFragment"     to R.id.action_illusionTestsFragment_to_resultsManagerFragment,
            "PredictionTestsFragment"   to R.id.action_predictionTestsFragment_to_resultsManagerFragment,
            "VariousTestsFragment"      to R.id.action_variousTestsFragment_to_resultsManagerFragment,
            "BindingsFragment"          to R.id.action_bindingsFragment_to_resultsManagerFragment
        )
    )
    
    /**
     * Fallback navigation actions when direct navigation is not available
     */
    private val fallbackMappings = mapOf(
        NavigationDestination.TEST_FRAGMENT to R.id.action_mainFragment_to_testFragment,
        NavigationDestination.RESULTS_MANAGER_FRAGMENT to R.id.action_mainFragment_to_resultsManagerFragment
    )
    
    /**
     * Resolves the navigation action from current fragment to target destination
     * 
     * @param activity The current activity containing the navigation host
     * @param destination The target navigation destination
     * @return Navigation action ID or null if not found
     */
    fun resolveNavigationAction(
        activity: Activity,
        destination: NavigationDestination
    ): Int? {
        val fragmentTag = getCurrentFragmentTag(activity)
        Log.d(TAG, "Resolving navigation action from fragment: $fragmentTag to destination: $destination")
        
        if (fragmentTag != null) {
            val actionId = getDirectNavigationAction(fragmentTag, destination)
            if (actionId != null) {
                Log.d(TAG, "Found direct navigation action: $actionId")
                return actionId
            }
        }
        
        // Fallback to default navigation action
        val fallbackActionId = getFallbackNavigationAction(destination)
        Log.d(TAG, "Using fallback navigation action: $fallbackActionId for destination: $destination")
        return fallbackActionId
    }
    
    /**
     * Attempts navigation with fallback strategies
     * 
     * @param navController The navigation controller
     * @param destination The target destination
     * @param activity The current activity
     * @return true if navigation succeeded, false otherwise
     */
    fun navigateWithFallback(
        navController: NavController,
        destination: NavigationDestination
    ): Boolean {
        try {
            // Try direct navigation to the fragment first
            try {
                val destinationId = when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.resultsManagerFragment
                }
                navController.navigate(destinationId)
                Log.i(TAG, "Successfully navigated directly to destination: $destination")
                return true
            } catch (e: IllegalArgumentException) {
                Log.d(TAG, "Direct navigation failed, trying with action", e)
            }
            
            // Try to get the appropriate action from current fragment
            val currentDestination = navController.currentDestination?.id
            val actionId = when (currentDestination) {
                R.id.mainFragment -> when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.action_mainFragment_to_testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_mainFragment_to_resultsManagerFragment
                }
                R.id.temporalTestsFragment -> when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.action_temporalTestsFragment_to_testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_temporalTestsFragment_to_resultsManagerFragment
                }
                R.id.illusionTestsFragment -> when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.action_illusionTestsFragment_to_testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_illusionTestsFragment_to_resultsManagerFragment
                }
                R.id.variousTestsFragment -> when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.action_variousTestsFragment_to_testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_variousTestsFragment_to_resultsManagerFragment
                }
                R.id.predictionTestsFragment -> when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.action_predictionTestsFragment_to_testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_predictionTestsFragment_to_resultsManagerFragment
                }
                R.id.bindingsFragment -> when (destination) {
                    NavigationDestination.TEST_FRAGMENT -> R.id.action_bindingsFragment_to_testFragment
                    NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_bindingsFragment_to_resultsManagerFragment
                }
                else -> {
                    // If no specific action available, try to go back to main fragment first
                    Log.d(TAG, "No direct action available from current fragment ($currentDestination), trying alternative approaches")
                    try {
                        // Try to pop back to main fragment
                        navController.popBackStack(R.id.mainFragment, false)
                        val fallbackActionId = when (destination) {
                            NavigationDestination.TEST_FRAGMENT -> R.id.action_mainFragment_to_testFragment
                            NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.action_mainFragment_to_resultsManagerFragment
                        }
                        navController.navigate(fallbackActionId)
                        Log.i(TAG, "Successfully navigated to $destination via main fragment")
                        return true
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to navigate via main fragment, trying direct navigation", e)
                        // Last resort: try direct navigation to the fragment
                        val destinationId = when (destination) {
                            NavigationDestination.TEST_FRAGMENT -> R.id.testFragment
                            NavigationDestination.RESULTS_MANAGER_FRAGMENT -> R.id.resultsManagerFragment
                        }
                        navController.navigate(destinationId)
                        Log.i(TAG, "Successfully navigated directly to $destination")
                        return true
                    }
                }
            }

            navController.navigate(actionId)
            Log.i(TAG, "Successfully navigated to $destination using action")
            return true

        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "Navigation action not found for destination: $destination", e)
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Navigation controller not ready for destination: $destination", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error navigating to destination: $destination", e)
        }
        
        Log.e(TAG, "All navigation attempts failed for destination: $destination")
        return false
    }
    
    /**
     * Gets current fragment class name for debugging
     * 
     * @param activity The current activity
     * @return Fragment class simple name or null if not found
     */
    fun getCurrentFragmentTag(activity: Activity): String? {
        return try {
            if (activity is AppCompatActivity) {
                val navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.my_nav_host_fragment)
                val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()
                val fragmentTag = currentFragment?.javaClass?.simpleName
                Log.d(TAG, "Current fragment tag: $fragmentTag")
                fragmentTag
            } else {
                Log.w(TAG, "Activity is not AppCompatActivity, cannot get fragment tag")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get current fragment tag", e)
            null
        }
    }
    
    /**
     * Gets direct navigation action for a specific fragment and destination
     * 
     * @param fragmentTag The source fragment class name
     * @param destination The target destination
     * @return Navigation action ID or null if not found
     */
    private fun getDirectNavigationAction(
        fragmentTag: String,
        destination: NavigationDestination
    ): Int? {
        return navigationMappings[destination]?.get(fragmentTag)
    }
    
    /**
     * Gets fallback navigation action for a destination
     * 
     * @param destination The target destination
     * @return Fallback navigation action ID
     */
    private fun getFallbackNavigationAction(
        destination: NavigationDestination
    ): Int? {
        return fallbackMappings[destination]
    }
}