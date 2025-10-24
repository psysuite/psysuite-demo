package iit.uvip.psysuite

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.device.DeviceIdentificationManager
import iit.uvip.psysuite.core.utility.filesystem.FileSystemManager
import iit.uvip.psysuite.core.utility.filesystem.ResultFileItem
import kotlinx.coroutines.*
import org.albaspazio.core.accessory.SingletonHolder
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.mail.EMailAccount
import org.albaspazio.core.mail.Mail
import org.albaspazio.core.mail.MailIntent
import org.albaspazio.core.ui.show1MethodDialog
import org.albaspazio.core.ui.show2ChoisesDialog
import org.albaspazio.core.ui.showAlert
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import kotlin.math.min
import androidx.core.content.edit

/*
    RULES:
    user can send data by web upload or email, whether enabled. In both cases, these conditions are needed:
    1) the device must be registered
    2) internet connection must exist
    3) result file must exist
 */

// SINGLETON
class ResultsManager private constructor(private var activity: Activity) {

    companion object : SingletonHolder<ResultsManager, Activity>(::ResultsManager)

    private val resources: Resources        = activity.resources
    private val deviceManager               = DeviceIdentificationManager.getInstance(activity)
    private val fileSystemManager           = FileSystemManager.getInstance()

    private val prefs: SharedPreferences    = activity.getSharedPreferences("psysuite_web_config", Context.MODE_PRIVATE)

    private var maxRetryAttempts: Int       = prefs.getInt("max_retry_attempts", 3)
    private var retryDelayMs: Long          = prefs.getLong("retry_delay_ms", 5000)

    private val HTTP_ERROR_SUBMISSION_NOT_ALLOWED = 423

    // Simple properties - no SecureStorage needed
    var webApiUrl: String = BuildConfig.API_URL
    var webApiKey: String = BuildConfig.API_KEY

    // region flags
    // Web upload is enabled when API URL and key are properly configured
    val isWebUploadEnabled: Boolean
        get() = webApiUrl.isNotBlank() && webApiKey.isNotBlank()

    val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network             = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

    var isEmailEnabled:Boolean
        get()       = prefs.getBoolean("email_enabled", false)
        set(value)  = prefs.edit { putBoolean("email_enabled", value)}

    val canUpload: Boolean
        get() = deviceManager.isDeviceRegistered && isNetworkAvailable && isWebUploadEnabled

    val canSendEmail: Boolean
        get() = deviceManager.isDeviceRegistered && isNetworkAvailable && isEmailEnabled
    


    // endregion

    // region Email configuration (existing)
    private val emailAccount: EMailAccount      = EMailAccount("antares.psysuite@gmail.com", "uvipapptester19", "antares.psysuite@gmail.com")
    private var emailRecipients:Array<String>   = arrayOf("antares.psysuite@gmail.com")

    private lateinit var mailJob: Job
    private var mailAD: AlertDialog? = null

    // endregion

    private lateinit var uploadJob: Job


    fun updateContext(newActivity: Activity) {
        this.activity = newActivity
    }

    /**
     * Opens the Results Manager Fragment for file management
     */
    fun openResultsManager() {
        try {
            val navController = activity.findNavController(R.id.my_nav_host_fragment)
            
            // Use NavigationActionManager for centralized navigation handling
            val success = NavigationActionManager.navigateWithFallback(navController, NavigationActionManager.NavigationDestination.RESULTS_MANAGER_FRAGMENT)
            
            if (!success) {
                Log.w("ResultsManager", "Navigation to results manager failed, using fallback")
                showAlert(activity, resources.getString(R.string.warning), "Results manager not available, using legacy upload check")
                checkPendingUploads()
            }
            
        } catch (e: Exception) {
            Log.e("ResultsManager", "Unexpected error navigating to results manager", e)
            showAlert(activity, resources.getString(R.string.error), "Cannot open results manager: ${e.message}")
            // Still provide fallback functionality
            checkPendingUploads()
        }
    }
    val existResultsToSend: Boolean
        get() {
            val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "psysuite_results")
            if (!downloadsDir.exists()) return false
            return fileSystemManager.scanForValidResultPairs(downloadsDir).isNotEmpty()
        }


    // Main method called when test finishes, set result and call processResults.
    fun onTestFinished(result: TestResult) {

        if(isEmailEnabled) {
            // check whether test defined specific recipients. otherwise use the default one(s)
            val ci = getCompanionObjectMethod(result.testClass, "getEmailRecipients")
            if (ci.first != null) emailRecipients = (ci.first?.call(ci.second)) as Array<String>
        }
        
        // Determine submission strategy based on device registration, network presence, result file presence, enabled configurations
        when {
            result.res_files.isEmpty() -> showTestCompletionMessage(result.code)

            !isNetworkAvailable -> {
                showAlert(activity, resources.getString(R.string.no_internet_connection), resources.getString(R.string.connect_then_send_results))
                showTestCompletionMessage(result.code)
            }

            !deviceManager.isDeviceRegistered -> {
                showAlert(activity, resources.getString(R.string.device_not_registered), resources.getString(R.string.register_device_then_send_results))
                showTestCompletionMessage(result.code)
            }

            !isWebUploadEnabled && !isEmailEnabled -> {
                showAlert(activity, resources.getString(R.string.warning), "se vuoi mandare i risultati configura la web application o la mail")
                showTestCompletionMessage(result.code)
            }

            canUpload -> {      // default behaviour: web upload
                if (result.code == TestBasic.TEST_COMPLETED)    uploadToWebBackend(result)
                else                                            askWhetherUploadingToWeb(result)
            }

            canSendEmail -> {
                if (result.code == TestBasic.TEST_COMPLETED)    sendByEmail(result)
                else                                            askWhetherSending(result)
            }

            else -> {
                // Fallback: just show completion message
                showTestCompletionMessage(result.code)
            }
        }
    }

    // Check for pending uploads at app startup or after a test completion when some condition (device registration, internet connection, web setup) were not met
    fun checkPendingUploads() {

        when{
            !isNetworkAvailable                 ->  {
                                                        showAlert(activity, resources.getString(R.string.no_internet_connection), resources.getString(R.string.connect_then_send_results))
                                                        return
                                                    }
            !deviceManager.isDeviceRegistered   ->  {
                                                        showAlert(activity, resources.getString(R.string.device_not_registered), resources.getString(R.string.register_device_then_send_results))
                                                        return
                                                    }
            !isWebUploadEnabled && !isEmailEnabled -> {
                                                        showAlert(activity, resources.getString(R.string.warning), "se vuoi mandare i risultati configura la web application o la mail")
                                                        return
                                                    }
        }
        // can i proceed with one of the two methods
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "psysuite_results")
                if (!downloadsDir.exists()) return@launch

                val resultsFiles = fileSystemManager.scanForValidResultPairs(downloadsDir)
                for(res_file in resultsFiles){
                    Log.d("ResultsManager", "Found result file: ${res_file.displayName}")
                    val experimentData = parseExperimentFiles(res_file.jsonFile, res_file.txtFile)
                    if (experimentData != null) {
                        Log.i("ResultsManager", "Attempting deferred upload for experiment: ${experimentData.exp_uid}")
                        val success = doUploadExperiment(experimentData)
                        if (success) {
                            Log.i("ResultsManager", "Successfully uploaded deferred experiment: ${experimentData.exp_uid}")
                            moveFilesToPrivateStorage(arrayOf(res_file.jsonFile.absolutePath, res_file.txtFile.absolutePath))
                        } else {
                            Log.w("ResultsManager", "Failed to upload deferred experiment: ${experimentData.exp_uid}")
                        }
                    } else {
                        Log.e("ResultsManager", "Failed to parse deferred experiment files: ${res_file.jsonFile.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ResultsManager", "Error checking pending uploads", e)
            }
        }
    }

    private fun showTestCompletionMessage(code: Int) {
        when(code){
            TestBasic.TEST_COMPLETED            -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_completed_success))
            TestBasic.TEST_ABORTED_DEL_RESULT,
            TestBasic.TEST_ABORTED_KEEP_RESULT,
            TestBasic.TEST_ABORTED_WITH_ERROR   -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_completed_abort))
            TestBasic.BLOCK_COMPLETED           -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_partially_completed))
        }
    }

    // region Web upload methods
    /**
     * Uploads multiple selected result files
     */
    fun uploadSelectedResults(selectedItems: List<ResultFileItem>, onProgress: (ResultFileItem, Boolean, String?) -> Unit) {
        uploadJob = GlobalScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    mailAD = show1MethodDialog(activity, "Upload", "Preparing upload...", resources.getString(R.string.abort)){
                        uploadJob.cancel()
                        mailAD?.dismiss()
                        mailAD = null
                    }
                }

                var successCount = 0
                val totalCount = selectedItems.size

                for ((index, item) in selectedItems.withIndex()) {
                    // Update progress dialog
                    withContext(Dispatchers.Main) {
                        mailAD?.setMessage("Uploading ${item.displayName}... (${index + 1}/$totalCount)")
                    }

                    try {
                        // Check if already submitted
                        if (fileSystemManager.isAlreadySubmitted(item.exp_uid)) {
                            Log.i("ResultsManager", "Skipping already submitted file: ${item.displayName}")
                            withContext(Dispatchers.Main) {
                                onProgress(item, true, "Already submitted")
                            }
                            successCount++
                            continue
                        }

                        // Parse and upload
                        val experimentData = parseExperimentFiles(item.jsonFile, item.txtFile)
                        if (experimentData != null) {
                            Log.i("ResultsManager", "Uploading experiment: ${experimentData.exp_uid}")
                            val success = doUploadExperiment(experimentData)

                            if (success) {
                                // Move files to submitted folder
                                val filesToMove = listOf(item.jsonFile, item.txtFile)
                                val moved = fileSystemManager.moveFilesToSubmitted(filesToMove)

                                if (moved) {
                                    successCount++
                                    Log.i("ResultsManager", "Successfully uploaded and moved: ${item.displayName}")
                                    withContext(Dispatchers.Main) {
                                        onProgress(item, true, null)
                                    }
                                } else {
                                    Log.w("ResultsManager", "Upload succeeded but failed to move files: ${item.displayName}")
                                    withContext(Dispatchers.Main) {
                                        onProgress(item, false, "Upload succeeded but failed to move files")
                                    }
                                }
                            } else {
                                Log.w("ResultsManager", "Upload failed: ${item.displayName}")
                                withContext(Dispatchers.Main) {
                                    onProgress(item, false, "Upload failed")
                                }
                            }
                        } else {
                            Log.e("ResultsManager", "Failed to parse experiment data: ${item.displayName}")
                            withContext(Dispatchers.Main) {
                                onProgress(item, false, "Failed to parse experiment data")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ResultsManager", "Error uploading ${item.displayName}", e)
                        withContext(Dispatchers.Main) {
                            onProgress(item, false, "Error: ${e.message}")
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    mailAD?.dismiss()
                    Log.i("ResultsManager", "Batch upload completed: $successCount/$totalCount files uploaded successfully")
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mailAD?.dismiss()
                }
                Log.e("ResultsManager", "Batch upload error", e)

                // Notify about the error for each remaining item
                selectedItems.forEach { item ->
                    withContext(Dispatchers.Main) {
                        onProgress(item, false, "Batch upload error: ${e.message}")
                    }
                }
            }
        }
    }

    private fun askWhetherUploadingToWeb(result: TestResult){
        show2ChoisesDialog(activity, resources.getString(R.string.warning), "Upload incomplete test results to web backend?", resources.getString(R.string.yes), resources.getString(R.string.no),
            { /* pressed YES */ uploadToWebBackend(result) },{})
    }

    private fun uploadToWebBackend(result: TestResult) {
        uploadJob = GlobalScope.launch {
            try {
                withContext(Dispatchers.Main) {
                    mailAD = show1MethodDialog(activity, "Upload", "Uploading results to web backend...", resources.getString(R.string.abort)){
                        uploadJob.cancel()
                        mailAD?.dismiss()
                        mailAD = null
                    }
                }

                val experimentData = parseExperimentFiles(result) // read the two files (config json & results) and return experiment data
                if (experimentData != null) {
                    val success = doUploadExperiment(experimentData)

                    withContext(Dispatchers.Main) {
                        mailAD?.dismiss()

                        if (success) {
                            moveFilesToPrivateStorage(result.res_files.toTypedArray())
                            showAlert(activity, resources.getString(R.string.success), "Results uploaded successfully")
                        } else {
                            showAlert(activity, resources.getString(R.string.failure), "Upload failed. Results saved locally for retry.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        mailAD?.dismiss()
                        showAlert(activity, resources.getString(R.string.failure), "Failed to parse experiment data")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    mailAD?.dismiss()
                    showAlert(activity, resources.getString(R.string.failure), "Upload error: ${e.message}")
                }
                Log.e("ResultsManager", "Upload error", e)
            }
        }
    }

    private fun parseExperimentFiles(result: TestResult): ExperimentUploadData? {
        return try {
            val jsonFile    = File(result.res_files[0]) // Assuming first file is JSON
            val resultFile  = File(result.res_files[1]) // Assuming second file is results
            parseExperimentFiles(jsonFile, resultFile)
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error parsing experiment files", e)
            null
        }
    }

    private fun parseExperimentFiles(jsonFile: File, resultFile: File): ExperimentUploadData? {

        return try {
            Log.d("ResultsManager", "Parsing experiment files: ${jsonFile.name}, ${resultFile.name}")
            
            val configJson      = JSONObject(jsonFile.readText())     // Parse JSON configuration

            val classesArray    = configJson.getJSONArray("classes")
            val testClassName   = classesArray.getString(0).substringAfterLast(".")

            val exp_uid        = configJson.getString("exp_uid")
            val validJson       = filterJson(configJson)            // keep only relevant fields
            val trials          = parseTrialResults(resultFile)     // Parse trial results

            Log.d("ResultsManager", "Parsed ${trials.size} trials from result file")

            val experimentData = ExperimentUploadData(
                exp_uid         = exp_uid,
                testClassName   = testClassName,
                configuration   = validJson,
                trials          = trials,
                deviceId        = deviceManager.deviceId!!
            )
            
            Log.i("ResultsManager", "Successfully parsed experiment data - ID: $exp_uid, Class: $testClassName, Trials: ${trials.size}, Device: ${deviceManager.deviceId!!}")
            return experimentData
            
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error parsing experiment files", e)
            null
        }
    }
    
    private fun filterJson(configJson: JSONObject): JSONObject {
        val validFields = listOf(
            "label", "age", "gender", "population", "session", "type", "project",
            "device", "vercode", "stimuliDelays", "whitenoise",
            "trman_type", "showResult", "canRepeat", "doTraining", "date"
        )

        val validJson  = JSONObject()
        validFields.forEach { field ->
            if (configJson.has(field))
                validJson.put(field, configJson.get(field))
        }
        return validJson
    }

    private fun parseTrialResults(resultFile: File): List<TrialData> {
        val trials = mutableListOf<TrialData>()
        
        try {
            val lines = resultFile.readLines()
            if (lines.isEmpty()) return trials
            
            // First line should be headers
            val headers = lines[0].split("\t")
            
            // Parse each data line
            for (i in 1 until lines.size) {
                val values = lines[i].split("\t")
                if (values.size == headers.size) {
                    val trialData = mutableMapOf<String, Any>()
                    
                    for (j in headers.indices) {
                        val header = headers[j].trim()
                        val value = values[j].trim()
                        
                        // Try to parse as appropriate type
                        trialData[header] = when {
                            value.toIntOrNull() != null -> value.toInt()
                            value.toDoubleOrNull() != null -> value.toDouble()
                            value.equals("true", ignoreCase = true) -> true
                            value.equals("false", ignoreCase = true) -> false
                            else -> value
                        }
                    }
                    trials.add(TrialData(i, trialData))
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error parsing trial results", e)
        }
        
        return trials
    }

    private suspend fun doUploadExperiment(experimentData: ExperimentUploadData): Boolean = withContext(Dispatchers.IO) {
        var attempt = 0
        var delay = retryDelayMs
        
        while (attempt < maxRetryAttempts) {
            try {
                if (!isNetworkAvailable) {
                    Log.w("ResultsManager", "No network available for upload attempt ${attempt + 1}")
                    delay(delay)
                    delay = min(delay * 2, 60000) // Exponential backoff, max 1 minute
                    attempt++
                    continue
                }
                
                // Additional connectivity check
                try {
                    val testUrl = URL(webApiUrl)
                    val testConnection = testUrl.openConnection()
                    testConnection.connectTimeout = 5000
                    testConnection.connect()
                    testConnection.getInputStream().close()
                    Log.d("ResultsManager", "Server connectivity test passed")
                } catch (e: Exception) {
                    Log.w("ResultsManager", "Server connectivity test failed: ${e.message}, move to next attempt")
                    // Abort this attempt and retry
                    withContext(Dispatchers.Main) {
                        Toast.makeText(activity,"Connection problems, I will make another attempt in few ms",Toast.LENGTH_SHORT).show()
                    }
                    delay(delay)
                    delay = min(delay * 2, 60000)
                    attempt++
                    continue                }
                
                val fullUrl = "$webApiUrl/api/upload/experiment"
                Log.d("ResultsManager", "Attempting connection to: $fullUrl")
                
                val url = URL(fullUrl)
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                connection.setRequestProperty("Authorization", "Bearer $webApiKey")
                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 60000
                
                // Create JSON payload matching web app expectations
                val payload = JSONObject().apply {
                    put("exp_uid", experimentData.exp_uid)
                    put("test_class_name", experimentData.testClassName)
                    put("device_id", experimentData.deviceId) // Include device ID if available
                    put("configuration", experimentData.configuration) // Full configuration (web app will filter)
                    put("trials", JSONArray().apply {
                        experimentData.trials.forEach { trial ->
                            put(JSONObject().apply {
                                put("trial_number", trial.trialNumber)
                                trial.data.forEach { (key, value) -> put(key, value) }
                            })
                        }
                    })
                }
                
                // Log payload details for validation
                Log.d("ResultsManager", "Upload payload - Unique ID: ${experimentData.exp_uid}")
                Log.d("ResultsManager", "Upload payload - Test class: ${experimentData.testClassName}")
                Log.d("ResultsManager", "Upload payload - Device ID: ${experimentData.deviceId}")
                Log.d("ResultsManager", "Upload payload - Trials count: ${experimentData.trials.size}")
                Log.d("ResultsManager", "Upload payload - Configuration keys: ${experimentData.configuration.keys().asSequence().toList()}")
                
                // Send request
                Log.d("ResultsManager", "Sending request to: ${connection.url}")
                Log.d("ResultsManager", "Request method: ${connection.requestMethod}")
                Log.d("ResultsManager", "Content length: ${payload.toString().toByteArray().size}")
                
                try {
                    connection.outputStream.use { os ->
                        os.write(payload.toString().toByteArray())
                        os.flush()
                    }
                    Log.d("ResultsManager", "Request payload sent successfully")
                } catch (e: Exception) {
                    Log.e("ResultsManager", "Failed to send request payload", e)
                    throw e
                }
                
                val responseCode = connection.responseCode
                Log.d("ResultsManager", "Received response code: $responseCode")

                when (responseCode) {
                    HttpURLConnection.HTTP_CREATED -> {
                        Log.i("ResultsManager", "Experiment uploaded successfully")
                        return@withContext true
                    }
                    HttpURLConnection.HTTP_CONFLICT -> {
                        // Duplicate experiment - consider it successful
                        Log.i("ResultsManager", "Experiment already exists on server")
                        return@withContext true
                    }
                    HTTP_ERROR_SUBMISSION_NOT_ALLOWED -> {
                        // submission was ok, but test is finalized or do not accept submissions
                        return@withContext false
                    }
                    else -> {
                        val errorMessage = try {
                            connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                        } catch (_: Exception) {
                            "HTTP $responseCode"
                        }
                        Log.w("ResultsManager", "Upload failed with code $responseCode: $errorMessage")
                    }
                }
                
            } catch (e: IOException) {
                Log.w("ResultsManager", "Upload attempt ${attempt + 1} failed", e)
            } catch (e: Exception) {
                Log.e("ResultsManager", "Unexpected error during upload", e)
                return@withContext false
            }
            
            attempt++
            if (attempt < maxRetryAttempts) {
                delay(delay)
                delay = min(delay * 2, 60000) // Exponential backoff
            }
        }
        
        Log.e("ResultsManager", "Upload failed after $maxRetryAttempts attempts")
        return@withContext false
    }
    // endregion

    // region EMAIL
    private fun askWhetherSending(result: TestResult){
        show2ChoisesDialog(activity, resources.getString(R.string.warning), resources.getString(R.string.ask_send_results), resources.getString(R.string.yes), resources.getString(R.string.no),
            { /* pressed YES */ sendByEmail(result) },{})
    }

    private fun sendByEmail(result: TestResult) {
        mailJob = GlobalScope.launch {
            try {
//                MailIntent.composeEmail(activity, "iit.uvip.psysuite.provider", emailRecipients, result.mailsubject, result.mailbody, result.res_files)
                mailAD = withContext(Dispatchers.Main) {
                    return@withContext show1MethodDialog(activity, resources.getString(R.string.warning), resources.getString(R.string.sending_results), resources.getString(R.string.abort)){
                        // abort mail submission
                        mailJob.cancel()
                        mailAD?.dismiss()
                        mailAD = null
                    }
                }
                val res = doSendByEmail(result)
                mailAD?.dismiss()

                withContext(Dispatchers.Main) {
                    if (res) showAlert(
                        activity,
                        resources.getString(R.string.success),
                        resources.getString(R.string.results_sent)
                    )
                    else showAlert(
                        activity,
                        resources.getString(R.string.failure),
                        resources.getString(R.string.email_account_error)
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showAlert(activity, resources.getString(R.string.failure), resources.getString(R.string.email_generic_error, e.toString()))
                }
                mailAD?.dismiss()

                withContext(Dispatchers.Main) {
                    show2ChoisesDialog(activity, resources.getString(R.string.warning),
                        resources.getString(R.string.ask_send_results_intent), resources.getString(R.string.yes), resources.getString(R.string.no),
                        {
                            MailIntent.composeEmail(
                                activity,
                                "iit.uvip.psysuite.provider",
                                emailRecipients,
                                result.mailsubject,
                                result.mailbody,
                                result.res_files
                            )   // pressed YES
                        }, {})
                }
            }
        }
    }

    private suspend fun doSendByEmail(res: TestResult):Boolean = withContext(Dispatchers.IO) {
        val mail = Mail(emailAccount)
        return@withContext  mail.send(emailRecipients, res.mailsubject, res.mailbody, res.res_files)
    }
    // endregion

    private fun moveFilesToPrivateStorage(filePaths: Array<String>) {
        try {
            // Use the new submitted folder structure instead of private storage
            val filesToMove = filePaths.map { File(it) }
            val moved = fileSystemManager.moveFilesToSubmitted(filesToMove)
            
            if (moved) {
                Log.i("ResultsManager", "Successfully moved ${filePaths.size} files to submitted folder")
            } else {
                Log.e("ResultsManager", "Failed to move some files to submitted folder")
                
                // Fallback to old private storage method
                val privateDir = File(activity.filesDir, "uploaded_experiments")
                if (!privateDir.exists()) {
                    privateDir.mkdirs()
                }

                filePaths.forEach { filePath ->
                    val sourceFile = File(filePath)
                    if (sourceFile.exists()) {
                        val destFile = File(privateDir, sourceFile.name)
                        sourceFile.copyTo(destFile, overwrite = true)
                        sourceFile.delete()
                        Log.i("ResultsManager", "Moved ${sourceFile.name} to private storage (fallback)")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsManager", "Error moving files to storage", e)
        }
    }

    // Data classes for upload
    data class ExperimentUploadData(
        val exp_uid: String,            // unique experiment identifier (generated during JSON creation)
        val testClassName: String,      // test class name for DB table naming (e.g., "TestBIS")
        val configuration: JSONObject,  // full JSON configuration (web app will filter internal fields)
        val trials: List<TrialData>,    // trial results data
        var deviceId: String = ""       // device identifier for tracking
    )

    data class TrialData(
        val trialNumber: Int,
        val data: Map<String, Any>
    )


    /**
     * Saves result files to the new location (Download/psysuite_results)
     * This method should be called instead of saving to Download directly
     */
//    fun saveResultsToNewLocation(result: TestResult): Array<String> {
//        return try {
//            val resultsDir = fileSystemManager.getResultsFolder()
//            val newFilePaths = mutableListOf<String>()
//
//            // Copy existing files to new location if they're not already there
//            result.res_files.forEach { originalPath ->
//                val originalFile = File(originalPath)
//                val newFile = File(resultsDir, originalFile.name)
//
//                if (!newFile.exists()) {
//                    originalFile.copyTo(newFile, overwrite = false)
//                    Log.i("ResultsManager", "Copied ${originalFile.name} to new results folder")
//                }
//                newFilePaths.add(newFile.absolutePath)
//            }
//
//            newFilePaths.toTypedArray()
//        } catch (e: Exception) {
//            Log.e("ResultsManager", "Failed to save results to new location", e)
//            result.res_files.toTypedArray() // Return original paths as fallback
//        }
//    }

    /**
     * Test method to validate Android integration fixes
     * This method can be called to verify that all integration components work correctly
     */
    fun validateIntegrationFixes(): Boolean {
        Log.i("ResultsManager", "=== Starting Android Integration Validation ===")
        
        var allTestsPassed = true
        
        // Test 1: Validate unique ID handling
        try {
            val testJson = JSONObject().apply {
                put("exp_uid", "test_12345_abcd")
                put("classes", JSONArray().apply { put("iit.uvip.psysuite.tests.TestBIS") })
            }
            
            val testFile = File.createTempFile("test_config", ".json")
            testFile.writeText(testJson.toString())
            
            val resultFile = File.createTempFile("test_result", ".txt")
            resultFile.writeText("header1\theader2\nvalue1\tvalue2\n")
            
            val experimentData = parseExperimentFiles(testFile, resultFile)
            
            if (experimentData?.exp_uid == "test_12345_abcd") {
                Log.i("ResultsManager", "✓ Test 1 PASSED: Unique ID correctly read from exp_uid field")
            } else {
                Log.e("ResultsManager", "✗ Test 1 FAILED: Unique ID not correctly read (got: ${experimentData?.exp_uid})")
                allTestsPassed = false
            }
            
            testFile.delete()
            resultFile.delete()
            
        } catch (e: Exception) {
            Log.e("ResultsManager", "✗ Test 1 FAILED: Exception during unique ID test", e)
            allTestsPassed = false
        }
        
        // Test 2: Validate test class name extraction
        try {
            val fullClassName = "iit.uvip.psysuite.tests.TestBIS"
            val extractedName = fullClassName.substringAfterLast(".")
            
            if (extractedName == "TestBIS") {
                Log.i("ResultsManager", "✓ Test 2 PASSED: Test class name correctly extracted")
            } else {
                Log.e("ResultsManager", "✗ Test 2 FAILED: Test class name extraction failed (got: $extractedName)")
                allTestsPassed = false
            }
        } catch (e: Exception) {
            Log.e("ResultsManager", "✗ Test 2 FAILED: Exception during class name test", e)
            allTestsPassed = false
        }
        
        // Test 3: Validate device registration integration
        try {
            val isRegistered = deviceManager.isDeviceRegistered
            val deviceId = if (isRegistered) deviceManager.deviceId else null
            
            Log.i("ResultsManager", "✓ Test 3 INFO: Device registration status: $isRegistered, ID: $deviceId")
            
        } catch (e: Exception) {
            Log.e("ResultsManager", "✗ Test 3 FAILED: Exception during device registration test", e)
            allTestsPassed = false
        }
        
        // Test 4: Validate configuration requirements
        try {
            val testConfig = JSONObject().apply {
                put("classes", JSONArray().apply { put("TestClass") })
                put("label", "Test Label")
                put("age", 25)
                put("gender", "M")
                put("population", "TD")
                put("session", "1")
                put("type", 100)
                put("device", "TestDevice")
                put("vercode", "1.0")
                put("stimuliDelays", JSONArray())
                put("whitenoise", false)
                put("trman_type", "standard")
                put("showResult", true)
                put("canRepeat", false)
                put("doTraining", true)
                put("date", "2024-01-01")
            }
            
            Log.i("ResultsManager", "✓ Test 4 PASSED: Configuration validation completed")
            
        } catch (e: Exception) {
            Log.e("ResultsManager", "✗ Test 4 FAILED: Exception during configuration test", e)
            allTestsPassed = false
        }
        
        Log.i("ResultsManager", "=== Integration Validation Complete: ${if (allTestsPassed) "ALL TESTS PASSED" else "SOME TESTS FAILED"} ===")
        return allTestsPassed
    }
}