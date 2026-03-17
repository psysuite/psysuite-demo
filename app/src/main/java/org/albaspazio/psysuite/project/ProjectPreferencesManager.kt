package org.albaspazio.psysuite.project

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Manages SharedPreferences storage for project data
 */
class ProjectPreferencesManager(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "psysuite_projects"
        private const val KEY_PROJECTS = "projects"
        private const val TAG = "ProjectPreferencesManager"
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Save projects to SharedPreferences
     */
    fun saveProjects(projects: Set<String>): Boolean {
        return try {
            prefs.edit()
                .putStringSet(KEY_PROJECTS, projects)
                .apply()
            Log.d(TAG, "Saved ${projects.size} projects to preferences")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving projects to preferences", e)
            false
        }
    }
    
    /**
     * Load projects from SharedPreferences
     */
    fun loadProjects(): Set<String> {
        return try {
            val projects = prefs.getStringSet(KEY_PROJECTS, emptySet()) ?: emptySet()
            Log.d(TAG, "Loaded ${projects.size} projects from preferences")
            projects
        } catch (e: Exception) {
            Log.e(TAG, "Error loading projects from preferences", e)
            emptySet()
        }
    }
    
    /**
     * Add a single project to the stored projects
     */
    fun addProject(projectName: String): Boolean {
        if (projectName.isBlank()) {
            Log.w(TAG, "Cannot add blank project name")
            return false
        }
        
        val currentProjects = loadProjects().toMutableSet()
        val added = currentProjects.add(projectName.trim())
        
        return if (added) {
            saveProjects(currentProjects)
        } else {
            Log.w(TAG, "Project '$projectName' already exists")
            false
        }
    }
    
    /**
     * Remove a project from the stored projects
     */
    fun removeProject(projectName: String): Boolean {
        val currentProjects = loadProjects().toMutableSet()
        val removed = currentProjects.remove(projectName)
        
        return if (removed) {
            saveProjects(currentProjects)
        } else {
            Log.w(TAG, "Project '$projectName' not found for removal")
            false
        }
    }
    
    /**
     * Update a project name
     */
    fun updateProject(oldName: String, newName: String): Boolean {
        if (newName.isBlank()) {
            Log.w(TAG, "Cannot update to blank project name")
            return false
        }
        
        val currentProjects = loadProjects().toMutableSet()
        
        if (!currentProjects.contains(oldName)) {
            Log.w(TAG, "Project '$oldName' not found for update")
            return false
        }
        
        if (currentProjects.contains(newName.trim()) && oldName != newName.trim()) {
            Log.w(TAG, "Project '$newName' already exists")
            return false
        }
        
        currentProjects.remove(oldName)
        currentProjects.add(newName.trim())
        
        return saveProjects(currentProjects)
    }
    
    /**
     * Check if a project exists
     */
    fun projectExists(projectName: String): Boolean {
        return loadProjects().contains(projectName)
    }
    
    /**
     * Clear all projects (for testing or reset purposes)
     */
    fun clearAllProjects(): Boolean {
        return try {
            prefs.edit().remove(KEY_PROJECTS).apply()
            Log.d(TAG, "Cleared all projects from preferences")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing projects from preferences", e)
            false
        }
    }
}