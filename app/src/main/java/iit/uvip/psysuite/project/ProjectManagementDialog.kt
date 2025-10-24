package iit.uvip.psysuite.project

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import iit.uvip.psysuite.R

/**
 * Dialog for managing projects (CRUD operations)
 */
class ProjectManagementDialog : DialogFragment() {

    private lateinit var projectManager: ProjectManager
    private lateinit var projectAdapter: ProjectAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextNewProject: EditText
    private lateinit var buttonAddProject: Button
    private lateinit var textViewEmptyState: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_project_management, null)

        setupViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadProjects()

        builder.setView(view)
        return builder.create()
    }

    private fun setupViews(view: View) {
        projectManager      = ProjectManager.getInstance(requireContext())
        
        recyclerView        = view.findViewById(R.id.recyclerViewProjects)
        editTextNewProject  = view.findViewById(R.id.editTextNewProject)
        buttonAddProject    = view.findViewById(R.id.buttonAddProject)
        textViewEmptyState  = view.findViewById(R.id.textViewEmptyState)
        
        val buttonClose = view.findViewById<Button>(R.id.buttonClose)
        buttonClose.setOnClickListener { dismiss() }
    }

    private fun setupRecyclerView() {
        projectAdapter = ProjectAdapter(
            projects = mutableListOf(),
            onEditClick = { projectName -> showEditProjectDialog(projectName) },
            onDeleteClick = { projectName -> showDeleteConfirmationDialog(projectName) }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = projectAdapter
        }
    }

    private fun setupClickListeners() {
        buttonAddProject.setOnClickListener {
            addNewProject()
        }
        
        editTextNewProject.setOnEditorActionListener { _, _, _ ->
            addNewProject()
            true
        }
    }

    private fun loadProjects() {
        val projects = projectManager.getUserProjects()
        projectAdapter.updateProjects(projects)
        updateEmptyState()
    }

    private fun addNewProject() {
        val projectName = editTextNewProject.text.toString().trim()
        
        if (projectName.isEmpty()) {
            showToast("Please enter a project name")
            return
        }
        
        val result = projectManager.addProject(projectName)
        
        when (result) {
            is ProjectResult.Success -> {
                showToast(result.message)
                editTextNewProject.text.clear()
                projectAdapter.addProject(projectName)
                updateEmptyState()
            }
            is ProjectResult.Error -> {
                showToast(result.message)
            }
        }
    }

    private fun showEditProjectDialog(projectName: String) {
        val editText = EditText(requireContext()).apply {
            setText(projectName)
            selectAll()
            maxLines = 1
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Project")
            .setMessage("Enter new project name:")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateProject(projectName, newName)
                } else {
                    showToast("Project name cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateProject(oldName: String, newName: String) {
        val result = projectManager.updateProject(oldName, newName)
        
        when (result) {
            is ProjectResult.Success -> {
                showToast(result.message)
                projectAdapter.updateProject(oldName, newName)
            }
            is ProjectResult.Error -> {
                showToast(result.message)
            }
        }
    }

    private fun showDeleteConfirmationDialog(projectName: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Project")
            .setMessage("Are you sure you want to delete '$projectName'?\n\nThis action cannot be undone. Existing experiments with this project will still retain the project name.")
            .setPositiveButton("Delete") { _, _ ->
                deleteProject(projectName)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteProject(projectName: String) {
        val result = projectManager.deleteProject(projectName)
        
        when (result) {
            is ProjectResult.Success -> {
                showToast(result.message)
                projectAdapter.removeProject(projectName)
                updateEmptyState()
            }
            is ProjectResult.Error -> {
                showToast(result.message)
            }
        }
    }

    private fun updateEmptyState() {
        val hasProjects = projectAdapter.itemCount > 0
        recyclerView.visibility = if (hasProjects) View.VISIBLE else View.GONE
        textViewEmptyState.visibility = if (hasProjects) View.GONE else View.VISIBLE
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        // Make dialog wider
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    companion object {
        const val TAG = "ProjectManagementDialog"
        
        fun newInstance(): ProjectManagementDialog {
            return ProjectManagementDialog()
        }
    }
}