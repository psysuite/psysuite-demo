package iit.uvip.psysuite.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import iit.uvip.psysuite.R
import iit.uvip.psysuite.ResultsManager
import iit.uvip.psysuite.core.utility.filesystem.FileSystemManager
import iit.uvip.psysuite.core.utility.filesystem.ResultFileItem
import kotlinx.coroutines.*
import org.albaspazio.core.fragments.BaseFragment
import org.albaspazio.core.ui.showAlert
import java.io.IOException

/**
 * Fragment for managing and uploading result files
 */
class ResultsManagerFragment : BaseFragment(
    layout = R.layout.fragment_results_manager,
    landscape = false,
    hideAndroidControls = false
) {
    override val LOG_TAG: String = "ResultsManagerFragment"

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ResultsFileAdapter
    private lateinit var textSelectedCount: TextView
    private lateinit var buttonSelectAll: Button
    private lateinit var buttonSelectNone: Button
    private lateinit var fabUpload: FloatingActionButton
    private lateinit var emptyStateLayout: LinearLayout

    private lateinit var fileSystemManager: FileSystemManager
    private lateinit var resultsManager: ResultsManager

    private var loadJob: Job? = null
    private var networkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_results_manager, container, false)

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_results)
        textSelectedCount = view.findViewById(R.id.text_selected_count)
        buttonSelectAll = view.findViewById(R.id.button_select_all)
        buttonSelectNone = view.findViewById(R.id.button_select_none)
        fabUpload = view.findViewById(R.id.fab_upload)
        emptyStateLayout = view.findViewById(R.id.text_empty_state)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize managers
        fileSystemManager = FileSystemManager.getInstance()
        resultsManager = ResultsManager.getInstance(requireActivity())

        setupRecyclerView()
        setupButtons()
        setupNetworkMonitoring()
        loadResultFiles()
    }

    private fun setupRecyclerView() {
        adapter = ResultsFileAdapter { selectedCount ->
            updateSelectionUI(selectedCount)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    private fun setupButtons() {
        buttonSelectAll.setOnClickListener {
            adapter.selectAll()
        }

        buttonSelectNone.setOnClickListener {
            adapter.selectNone()
        }

        fabUpload.setOnClickListener {
            uploadSelectedFiles()
        }

        // Initially disable upload button
        updateSelectionUI(0)
    }

    private fun loadResultFiles() {
        loadJob?.cancel()
        loadJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                val resultFiles = fileSystemManager.scanForValidResultPairs(fileSystemManager.getResultsFolder())

                withContext(Dispatchers.Main) {
                    if (resultFiles.isEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        adapter.updateFiles(resultFiles)
                        updateSelectionUI(0)
                    }
                }
            } catch (e:SecurityException) {
                withContext(Dispatchers.Main) {
                    showStoragePermissionError()
                }
            } catch (e: IOException) {
                withContext(Dispatchers.Main) {
                    showStorageAccessError(e)
                }
            } catch (e:Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("ResultsManagerFragment", "Unexpected error loading files", e)
                    showGenericError("Failed to load result files", e.message)
                    showEmptyState()
                }
            }
        }
    }

    private fun showStoragePermissionError() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Storage Permission Required")
            .setMessage("PsySuite needs storage permission to access result files. Please grant storage permission in your device settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                try {
                    val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = android.net.Uri.parse("package:${requireContext().packageName}")
                    startActivity(intent)
                } catch (e: Exception) {
                    showAlert(requireActivity(), "Error", "Could not open settings. Please manually grant storage permission to PsySuite.")
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                showEmptyState()
            }
            .show()
    }

    private fun showStorageAccessError(e: java.io.IOException) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Storage Access Error")
            .setMessage("Cannot access the results folder. This might be due to:\n\n• Storage device not available\n• Folder permissions issue\n• Storage device full\n\nError: ${e.message}")
            .setPositiveButton("Retry") { _, _ ->
                loadResultFiles()
            }
            .setNegativeButton("Cancel") { _, _ ->
                showEmptyState()
            }
            .show()
    }

    private fun showGenericError(title: String, message: String?) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message ?: "An unexpected error occurred")
            .setPositiveButton("Retry") { _, _ ->
                loadResultFiles()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        buttonSelectAll.isEnabled = false
        buttonSelectNone.isEnabled = false
        fabUpload.isEnabled = false
        updateSelectionUI(0)
    }

    private fun hideEmptyState() {
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        buttonSelectAll.isEnabled = true
        buttonSelectNone.isEnabled = true
    }

    private fun updateSelectionUI(selectedCount: Int) {
        val stats = adapter.getSelectionStats()

        textSelectedCount.text = if (stats.hasSelection) {
            "${stats.selectedCount} of ${stats.totalCount} file${if (stats.totalCount != 1) "s" else ""} selected"
        } else {
            "No files selected"
        }

        // Re-evaluate canUpload dynamically to check current network status
        updateUploadButtonState(stats)

        // Update button text based on selection state
        buttonSelectAll.text = if (stats.isAllSelected) "Deselect All" else "Select All"

        // Update button click behavior
        buttonSelectAll.setOnClickListener {
            if (stats.isAllSelected) {
                adapter.selectNone()
            } else {
                adapter.selectAll()
            }
        }
    }

    private fun updateUploadButtonState(stats: ResultsFileAdapter.SelectionStats) {
        val canUploadNow = resultsManager.canUpload
        fabUpload.isEnabled = stats.hasSelection && canUploadNow

        // Update FAB appearance based on selection and upload capability
        fabUpload.alpha = if (stats.hasSelection && canUploadNow) 1.0f else 0.5f

        // Update the selection text to show upload status if needed
        if (stats.hasSelection && !canUploadNow) {
            textSelectedCount.text = "${stats.selectedCount} of ${stats.totalCount} file${if (stats.totalCount != 1) "s" else ""} selected (Upload not available)"
        }
    }

    private fun uploadSelectedFiles() {
        val selectedFiles = adapter.getSelectedFiles()
        if (selectedFiles.isEmpty()) {
            showAlert(requireActivity(), "No Selection", "Please select files to upload")
            return
        }

        // Force refresh the upload state before checking
        updateSelectionUI(adapter.getSelectedCount())

        // Check upload conditions with detailed error messages
        if (!resultsManager.canUpload) {
            showUploadConditionsError()
            return
        }

        // Show confirmation dialog
        val fileCount = selectedFiles.size
        val confirmMessage = "Upload $fileCount file${if (fileCount != 1) "s" else ""}?\n\nThis will upload the selected result files to the server."

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Upload")
            .setMessage(confirmMessage)
            .setPositiveButton("Upload") { _, _ ->
                performUpload(selectedFiles)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performUpload(selectedFiles: List<ResultFileItem>) {
        // Disable UI during upload
        setUploadingState(true)

        var completedCount = 0
        var successCount = 0
        val totalCount = selectedFiles.size
        val uploadedFiles = mutableListOf<ResultFileItem>()

        resultsManager.uploadSelectedResults(selectedFiles) { fileItem, success, errorMessage ->
            // This callback runs on the main thread
            completedCount++

            if (success) {
                successCount++
                uploadedFiles.add(fileItem)

                // Update progress in selection text
                textSelectedCount.text = "Uploading... ($completedCount/$totalCount completed)"
            } else {
                // Log error but don't show individual alerts during batch upload
                val message = errorMessage ?: "Upload failed"
                Log.w("ResultsManagerFragment", "Failed to upload ${fileItem.displayName}: $message")
            }

            // Check if all uploads are complete
            if (completedCount >= totalCount) {
                handleUploadCompletion(uploadedFiles, successCount, totalCount)
            }
        }
    }

    private fun handleUploadCompletion(uploadedFiles: List<ResultFileItem>, successCount: Int, totalCount: Int) {
        // Re-enable UI
        setUploadingState(false)

        // Remove successfully uploaded files from the list
        if (uploadedFiles.isNotEmpty()) {
            adapter.removeFiles(uploadedFiles)
        }

        // Check if list is now empty
        if (adapter.getCurrentFiles().isEmpty()) {
            showEmptyState()
        } else {
            // Update selection UI
            updateSelectionUI(adapter.getSelectedCount())
        }

        // Show completion message with appropriate actions
        when {
            successCount == totalCount -> {
                showAlert(requireActivity(), "Upload Complete",
                    "All $totalCount file${if (totalCount != 1) "s" else ""} uploaded successfully!")
            }
            successCount > 0 -> {
                val failedCount = totalCount - successCount
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Upload Partially Complete")
                    .setMessage("$successCount of $totalCount file${if (totalCount != 1) "s" else ""} uploaded successfully.\n\n$failedCount file${if (failedCount != 1) "s" else ""} failed to upload.")
                    .setPositiveButton("Retry Failed") { _, _ ->
                        // Retry only the failed uploads
                        val remainingFiles = adapter.getCurrentFiles().filter { it.isSelected }
                        if (remainingFiles.isNotEmpty()) {
                            performUpload(remainingFiles)
                        }
                    }
                    .setNegativeButton("OK", null)
                    .show()
            }
            else -> {
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Upload Failed")
                    .setMessage("Upload failed for all selected files. This could be due to:\n\n• Network connection issues\n• Server problems\n• File corruption\n\nWould you like to try again?")
                    .setPositiveButton("Retry") { _, _ ->
                        val selectedFiles = adapter.getSelectedFiles()
                        if (selectedFiles.isNotEmpty()) {
                            performUpload(selectedFiles)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun setUploadingState(isUploading: Boolean) {
        buttonSelectAll.isEnabled = !isUploading && adapter.getCurrentFiles().isNotEmpty()
        buttonSelectNone.isEnabled = !isUploading && adapter.getCurrentFiles().isNotEmpty()
        fabUpload.isEnabled = !isUploading && adapter.getSelectedCount() > 0

        if (isUploading) {
            fabUpload.alpha = 0.5f
        } else {
            updateSelectionUI(adapter.getSelectedCount())
        }
    }

    private fun setupNetworkMonitoring() {
        val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager

        networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                // Network became available
                requireActivity().runOnUiThread {
                    Log.d("ResultsManagerFragment", "Network became available")
                    updateSelectionUI(adapter.getSelectedCount())
                }
            }

            override fun onLost(network: android.net.Network) {
                // Network was lost
                requireActivity().runOnUiThread {
                    Log.d("ResultsManagerFragment", "Network was lost")
                    updateSelectionUI(adapter.getSelectedCount())
                }
            }
        }

        try {
            val networkRequest = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback!!)
            Log.d("ResultsManagerFragment", "Network callback registered")
        } catch (e: Exception) {
            Log.e("ResultsManagerFragment", "Failed to register network callback", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        loadJob?.cancel()

        // Unregister network callback
        networkCallback?.let { callback ->
            try {
                val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                connectivityManager.unregisterNetworkCallback(callback)
                Log.d("ResultsManagerFragment", "Network callback unregistered")
            } catch (e: Exception) {
                Log.e("ResultsManagerFragment", "Failed to unregister network callback", e)
            }
        }
    }

    private fun showUploadConditionsError() {
        val issues = mutableListOf<String>()
        
        if (!resultsManager.isWebUploadEnabled) {
            issues.add("• Web upload is not configured")
        }
        
        if (!resultsManager.isNetworkAvailable) {
            issues.add("• No internet connection available")
        }
        
        // Check device registration
        try {
            val deviceManager = iit.uvip.psysuite.device.DeviceIdentificationManager.getInstance(requireActivity())
            if (!deviceManager.isDeviceRegistered) {
                issues.add("• Device is not registered")
            }
        } catch (e: Exception) {
            issues.add("• Cannot verify device registration")
        }
        
        val message = "Cannot upload files. Please resolve these issues:\n\n${issues.joinToString("\n")}"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Upload Not Available")
            .setMessage(message)
            .setPositiveButton("Check Settings") { _, _ ->
                // Navigate to settings or show guidance
                try {
                    val intent = android.content.Intent(requireContext(), iit.uvip.psysuite.settings.SettingsActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    showAlert(requireActivity(), "Error", "Could not open settings")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Refresh the file list (can be called from outside)
     */
    fun refreshFiles() {
        loadResultFiles()
    }
    
    /**
     * Refresh the upload state (can be called from outside)
     */
    fun refreshUploadState() {
        updateSelectionUI(adapter.getSelectedCount())
    }
}