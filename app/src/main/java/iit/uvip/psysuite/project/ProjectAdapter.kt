package iit.uvip.psysuite.project

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import iit.uvip.psysuite.R

/**
 * RecyclerView adapter for displaying and managing projects
 */
class ProjectAdapter(
    private var projects: MutableList<String>,
    private val onEditClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewProjectName: TextView = itemView.findViewById(R.id.textViewProjectName)
        val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEditProject)
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDeleteProject)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        
        holder.textViewProjectName.text = project
        
        holder.buttonEdit.setOnClickListener {
            onEditClick(project)
        }
        
        holder.buttonDelete.setOnClickListener {
            onDeleteClick(project)
        }
    }

    override fun getItemCount(): Int = projects.size

    /**
     * Update the projects list and refresh the adapter
     */
    fun updateProjects(newProjects: List<String>) {
        projects.clear()
        projects.addAll(newProjects)
        notifyDataSetChanged()
    }

    /**
     * Add a project to the list
     */
    fun addProject(project: String) {
        projects.add(project)
        projects.sort()
        notifyDataSetChanged()
    }

    /**
     * Remove a project from the list
     */
    fun removeProject(project: String) {
        val index = projects.indexOf(project)
        if (index != -1) {
            projects.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    /**
     * Update a project name in the list
     */
    fun updateProject(oldName: String, newName: String) {
        val index = projects.indexOf(oldName)
        if (index != -1) {
            projects[index] = newName
            projects.sort()
            notifyDataSetChanged()
        }
    }
}