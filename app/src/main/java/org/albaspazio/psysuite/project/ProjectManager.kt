package org.albaspazio.psysuite.project

import android.content.Context
import android.util.Log
import org.albaspazio.core.accessory.SingletonHolder

/**
 * Central manager for project operations in the Android app
 */
class ProjectManager private constructor(private val context: Context) {
    
    companion object : SingletonHolder<ProjectManager, Context>(::ProjectManager) {
        private const val TAG = "ProjectManager"
        
        // Reserved project names that cannot be used by users
        private val RESERVED_NAMES = setOf("Select project", "n.a.", "select project", "N.A.", "na", "NA")
    }
    
    private val preferencesManager = ProjectPreferencesManager(context)
    
    /**
     * Get all projects including default options
     */
    fun getAllProjects(): List<String> {
        val defaultProjects = getDefaultProjects()
        val userProjects = getUserProjects().sorted()
        return defaultProjects + userProjects
    }
    
    /**
     * Get only user-created projects (excluding defaults)
     */
    fun getUserProjects(): List<String> {
        return preferencesManager.loadProjects()
            .filter { !isReservedName(it) }
            .sorted()
    }
    
    /**
     * Get default project options
     */
    fun getDefaultProjects(): List<String> {
        return listOf("Select project", "n.a.")
    }
    
    /**
     * Add a new project
     */
    fun addProject(projectName: String): ProjectResult {
        val trimmedName = projectName.trim()
        
        // Validation
        if (trimmedName.isBlank()) {
            return ProjectResult.Error("Project name cannot be empty")
        }
        
        if (isReservedName(trimmedName)) {
            return ProjectResult.Error("'$trimmedName' is a reserved name and cannot be used")
        }
        
        if (trimmedName.length > 50) {
            return ProjectResult.Error("Project name cannot exceed 50 characters")
        }
        
        // Check for invalid characters
        if (trimmedName.contains(Regex("[<>:\"/\\\\|?*]"))) {
            return ProjectResult.Error("Project name contains invalid characters")
        }
        
        val success = preferencesManager.addProject(trimmedName)
        return if (success) {
            Log.i(TAG, "Successfully added project: $trimmedName")
            ProjectResult.Success("Project '$trimmedName' added successfully")
        } else {
            ProjectResult.Error("Project '$trimmedName' already exists")
        }
    }
    
    /**
     * Update an existing project
     */
    fun updateProject(oldName: String, newName: String): ProjectResult {
        val trimmedNewName = newName.trim()
        
        // Validation
        if (trimmedNewName.isBlank()) {
            return ProjectResult.Error("Project name cannot be empty")
        }
        
        if (isReservedName(trimmedNewName)) {
            return ProjectResult.Error("'$trimmedNewName' is a reserved name and cannot be used")
        }
        
        if (trimmedNewName.length > 50) {
            return ProjectResult.Error("Project name cannot exceed 50 characters")
        }
        
        // Check for invalid characters
        if (trimmedNewName.contains(Regex("[<>:\"/\\\\|?*]"))) {
            return ProjectResult.Error("Project name contains invalid characters")
        }
        
        val success = preferencesManager.updateProject(oldName, trimmedNewName)
        return if (success) {
            Log.i(TAG, "Successfully updated project: $oldName -> $trimmedNewName")
            ProjectResult.Success("Project updated successfully")
        } else {
            ProjectResult.Error("Failed to update project. It may not exist or the new name already exists.")
        }
    }
    
    /**
     * Delete a project
     */
    fun deleteProject(projectName: String): ProjectResult {
        if (isReservedName(projectName)) {
            return ProjectResult.Error("Cannot delete reserved project names")
        }
        
        val success = preferencesManager.removeProject(projectName)
        return if (success) {
            Log.i(TAG, "Successfully deleted project: $projectName")
            ProjectResult.Success("Project '$projectName' deleted successfully")
        } else {
            ProjectResult.Error("Project '$projectName' not found")
        }
    }
    
    /**
     * Check if a project name is valid for selection (not "Select project")
     */
    fun isValidSelection(projectName: String?): Boolean {
        return projectName != null && 
               projectName != "Select project" && 
               projectName.isNotBlank()
    }
    
    /**
     * Check if a project name is reserved
     */
    private fun isReservedName(name: String): Boolean {
        return RESERVED_NAMES.any { it.equals(name, ignoreCase = true) }
    }
    
    /**
     * Get project count (excluding defaults)
     */
    fun getProjectCount(): Int {
        return getUserProjects().size
    }
    
    /**
     * Check if a project exists
     */
    fun projectExists(projectName: String): Boolean {
        return preferencesManager.projectExists(projectName)
    }
    
    /**
     * Clear all user projects (for testing or reset)
     */
    fun clearAllProjects(): ProjectResult {
        val success = preferencesManager.clearAllProjects()
        return if (success) {
            Log.i(TAG, "Successfully cleared all projects")
            ProjectResult.Success("All projects cleared")
        } else {
            ProjectResult.Error("Failed to clear projects")
        }
    }
}

/**
 * Result class for project operations
 */
sealed class ProjectResult {
    data class Success(val message: String) : ProjectResult()
    data class Error(val message: String) : ProjectResult()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}