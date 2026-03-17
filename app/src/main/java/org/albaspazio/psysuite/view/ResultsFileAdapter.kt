package org.albaspazio.psysuite.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.albaspazio.psysuite.R
import org.albaspazio.psysuite.utility.filesystem.ResultFileItem

/**
 * RecyclerView adapter for displaying result files with selection functionality
 */
class ResultsFileAdapter(
    private var resultFiles: MutableList<ResultFileItem> = mutableListOf(),
    private val onSelectionChanged: (Int) -> Unit = {}
) : RecyclerView.Adapter<ResultsFileAdapter.ResultFileViewHolder>() {

    /**
     * ViewHolder for result file items
     */
    class ResultFileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_select_file)
        val textFileName: TextView = itemView.findViewById(R.id.text_file_name)
        val textFileDate: TextView = itemView.findViewById(R.id.text_file_date)
        val textTrialCount: TextView = itemView.findViewById(R.id.text_trial_count)
        val textFileSize: TextView = itemView.findViewById(R.id.text_file_size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultFileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result_file, parent, false)
        return ResultFileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultFileViewHolder, position: Int) {
        val item = resultFiles[position]
        
        holder.textFileName.text = item.displayName
        holder.textFileDate.text = item.formattedDate
        holder.textTrialCount.text = item.formattedTrialCount
        holder.textFileSize.text = item.formattedSize
        
        // Set checkbox state without triggering listener
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isSelected
        
        // Update visual appearance based on selection
        updateItemAppearance(holder, item.isSelected)
        
        // Set checkbox listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            updateItemAppearance(holder, isChecked)
            onSelectionChanged(getSelectedCount())
        }
        
        // Make the entire item clickable to toggle selection
        holder.itemView.setOnClickListener {
            holder.checkBox.isChecked = !holder.checkBox.isChecked
        }
    }

    /**
     * Updates the visual appearance of an item based on selection state
     */
    private fun updateItemAppearance(holder: ResultFileViewHolder, isSelected: Boolean) {
        val alpha = if (isSelected) 1.0f else 0.7f
        holder.itemView.alpha = alpha
        
        // You can add more visual feedback here if needed
        // For example, changing background color, text color, etc.
    }

    override fun getItemCount(): Int = resultFiles.size

    /**
     * Updates the list of result files
     */
    fun updateFiles(newFiles: List<ResultFileItem>) {
        resultFiles.clear()
        resultFiles.addAll(newFiles)
        notifyDataSetChanged()
    }

    /**
     * Gets the count of currently selected files
     */
    fun getSelectedCount(): Int {
        return resultFiles.count { it.isSelected }
    }

    /**
     * Gets the list of currently selected files
     */
    fun getSelectedFiles(): List<ResultFileItem> {
        return resultFiles.filter { it.isSelected }
    }

    /**
     * Selects all files
     */
    fun selectAll() {
        resultFiles.forEach { it.isSelected = true }
        notifyDataSetChanged()
        onSelectionChanged(getSelectedCount())
    }

    /**
     * Deselects all files
     */
    fun selectNone() {
        resultFiles.forEach { it.isSelected = false }
        notifyDataSetChanged()
        onSelectionChanged(getSelectedCount())
    }

    /**
     * Toggles selection for all files
     */
    fun toggleSelectAll() {
        val hasSelected = resultFiles.any { it.isSelected }
        if (hasSelected) {
            selectNone()
        } else {
            selectAll()
        }
    }

    /**
     * Removes files from the list (used after successful upload)
     */
    fun removeFiles(filesToRemove: List<ResultFileItem>) {
        filesToRemove.forEach { fileToRemove ->
            val index = resultFiles.indexOfFirst { it.exp_uid == fileToRemove.exp_uid }
            if (index >= 0) {
                resultFiles.removeAt(index)
                notifyItemRemoved(index)
            }
        }
        onSelectionChanged(getSelectedCount())
    }

    /**
     * Gets the current list of files
     */
    fun getCurrentFiles(): List<ResultFileItem> {
        return resultFiles.toList()
    }

    /**
     * Gets selection statistics
     */
    fun getSelectionStats(): SelectionStats {
        val totalCount = resultFiles.size
        val selectedCount = getSelectedCount()
        return SelectionStats(totalCount, selectedCount)
    }

    /**
     * Data class for selection statistics
     */
    data class SelectionStats(
        val totalCount: Int,
        val selectedCount: Int
    ) {
        val hasSelection: Boolean get() = selectedCount > 0
        val isAllSelected: Boolean get() = selectedCount == totalCount && totalCount > 0
        val selectionText: String get() = "$selectedCount of $totalCount selected"
    }
}