package iit.uvip.psysuite

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import iit.uvip.psysuite.core.tests.TestBasic
import iit.uvip.psysuite.core.utility.TestResult
import iit.uvip.psysuite.device.DeviceIdentificationManager
import kotlinx.coroutines.*
import org.albaspazio.core.accessory.SingletonHolder
import org.albaspazio.core.accessory.getCompanionObjectMethod
import org.albaspazio.core.mail.EMailAccount
import org.albaspazio.core.mail.Mail
import org.albaspazio.core.mail.MailIntent
//import org.albaspazio.core.mail.EMailAccount
//import org.albaspazio.core.mail.Mail
//import org.albaspazio.core.mail.MailIntent
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

// SINGLETON
class ResultsManager private constructor(private val activity: Activity) {

    companion object : SingletonHolder<ResultsManager, Activity>(::ResultsManager)

    private val resources: Resources        = activity.resources
    private val prefs: SharedPreferences    = activity.getSharedPreferences("psysuite_web_config", Context.MODE_PRIVATE)
    private val deviceManager               = DeviceIdentificationManager.getInstance(activity)
    
    private var sendResult:Boolean = prefs.getBoolean("email_enabled", false)

    // Web upload configuration
    private var webApiUrl: String = prefs.getString("web_api_url", "https://your-server.com/api") ?: "https://your-server.com/api"
    private var webApiKey: String = prefs.getString("web_api_key", "") ?: ""
    
    // Web upload is enabled when API URL and key are properly configured
    private val webUploadEnabled: Boolean
        get() = webApiUrl.isNotBlank() && 
                webApiUrl != "https://your-server.com/api" && 
                webApiKey.isNotBlank()

    private var maxRetryAttempts: Int   = prefs.getInt("max_retry_attempts", 3)
    private var retryDelayMs: Long      = prefs.getLong("retry_delay_ms", 5000)

    // Email configuration (existing)
    private val emailAccount: EMailAccount      = EMailAccount("antares.psysuite@gmail.com", "uvipapptester19", "antares.psysuite@gmail.com")
    private var emailRecipients:Array<String>   = arrayOf("antares.psysuite@gmail.com")

    private lateinit var mailJob: Job
    private var mailAD: AlertDialog? = null
    private lateinit var uploadJob: Job

    // Configuration methods
    fun setWebApiUrl(url: String) {
        webApiUrl = url
        prefs.edit { putString("web_api_url", url) }
    }

    fun setWebApiKey(key: String) {
        webApiKey = key
        prefs.edit { putString("web_api_key", key) }
    }
    
    fun setEmailEnabled(enabled: Boolean) {
        sendResult = enabled
        prefs.edit { putBoolean("email_enabled", enabled) }
    }
    
    fun isEmailEnabled(): Boolean {
        return sendResult
    }
    
    fun isWebUploadEnabled(): Boolean {
        return webUploadEnabled
    }
    
    fun getWebApiUrl(): String {
        return webApiUrl
    }
    
    fun getWebApiKey(): String {
        return webApiKey
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // Main method called when test finishes
    fun onTestFinished(result: TestResult){

        // check whether test defined specific recipients. otherwise use the default one(s)
        val ci = getCompanionObjectMethod(result.testClass, "getEmailRecipients")
        if(ci.first != null) emailRecipients = (ci.first?.call(ci.second)) as Array<String>
        
        // Skip processing if no result files
        if (result.res_files.isEmpty()) {
            showTestCompletionMessage(result.code)
            return
        }
        
        // Determine submission strategy based on device registration, network, and configuration
        val isDeviceRegistered = deviceManager.isDeviceRegistered
        val isNetworkAvailable = isNetworkAvailable()
        val isEmailEnabled = sendResult
        
        // Add device ID to the result if device is registered
//        if (isDeviceRegistered) {
//            result.subject.deviceId = deviceManager.getDeviceId()!!
//        }
        
        when {
            // Device registered: Try web upload first, then email fallback
            isDeviceRegistered && webUploadEnabled && isNetworkAvailable -> {
                if (result.code == TestBasic.TEST_COMPLETED) {
                    uploadToWebBackend(result)
                } else {
                    askWhetherUploadingToWeb(result)
                }
            }
            
            // Device registered but no network: Try email if enabled
            isDeviceRegistered && webUploadEnabled && !isNetworkAvailable && isEmailEnabled -> {
                showNoNetworkDialog(result, canTryEmail = true)
            }
            
            // Device registered but no network and no email: Save locally
            isDeviceRegistered && webUploadEnabled && !isNetworkAvailable && !isEmailEnabled -> {
                showNoNetworkDialog(result, canTryEmail = false)
            }
            
            // Device registered but web upload disabled: Use email if enabled
            isDeviceRegistered && !webUploadEnabled && isEmailEnabled -> {
                if (result.code == TestBasic.TEST_COMPLETED) {
                    sendResult(result)
                } else {
                    askWhetherSending(result)
                }
            }
            
            // Device registered but no upload options available
            isDeviceRegistered && !webUploadEnabled && !isEmailEnabled -> {
                showAlert(activity, "Results Saved", "Results saved locally. No upload method configured.")
            }
            
            // Device not registered: Ask user what to do
            !isDeviceRegistered && isEmailEnabled -> {
                askUnregisteredDeviceAction(result)
            }
            
            // Device not registered and no email: Suggest registration
            !isDeviceRegistered && !isEmailEnabled -> {
                showDeviceRegistrationRequiredDialog(result)
            }
            
            else -> {
                // Fallback: just show completion message
                showTestCompletionMessage(result.code)
            }
        }
    }

    // Check for pending uploads at app startup
    fun checkPendingUploads() {
        if (!webUploadEnabled) return

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val downloadsDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "psysuite_data")
                if (!downloadsDir.exists()) return@launch

                val jsonFiles = downloadsDir.listFiles { file -> file.name.endsWith(".json") }
                jsonFiles?.forEach { jsonFile ->
                    val resultFile = File(jsonFile.parent, jsonFile.nameWithoutExtension + ".txt")
                    if (resultFile.exists()) {
                        val experimentData = parseExperimentFiles(jsonFile, resultFile)
                        if (experimentData != null) {
                            val success = doUploadExperiment(experimentData)
                            if (success) {
                                moveFilesToPrivateStorage(arrayOf(jsonFile.absolutePath, resultFile.absolutePath))
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ResultsManager", "Error checking pending uploads", e)
            }
        }
    }

    private fun askWhetherUploadingToWeb(result: TestResult){
        show2ChoisesDialog(activity, resources.getString(R.string.warning), "Upload incomplete test results to web backend?", resources.getString(R.string.yes), resources.getString(R.string.no),
            { /* pressed YES */ uploadToWebBackend(result) },{})
    }

    private fun askWhetherSending(result: TestResult){
        show2ChoisesDialog(activity, resources.getString(R.string.warning), resources.getString(R.string.ask_send_results), resources.getString(R.string.yes), resources.getString(R.string.no),
            { /* pressed YES */ sendResult(result) },{})
    }
    
    private fun askUnregisteredDeviceAction(result: TestResult) {
        show2ChoisesDialog(
            activity, 
            resources.getString(R.string.warning), 
            "Device not registered. Would you like to send results via email instead?", 
            resources.getString(R.string.yes), 
            resources.getString(R.string.no),
            { 
                // User chose to send via email
                if(result.res_files.isNotEmpty()) {
                    if(result.code == TestBasic.TEST_COMPLETED) sendResult(result)
                    else askWhetherSending(result)
                } else {
                    showTestCompletionMessage(result.code)
                }
            },
            { 
                // User chose not to send - just show completion message
                showTestCompletionMessage(result.code)
            }
        )
    }
    
    private fun showTestCompletionMessage(code: Int) {
        when(code){
            TestBasic.TEST_COMPLETED -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_completed_success))
            TestBasic.TEST_ABORTED_DEL_RESULT,
            TestBasic.TEST_ABORTED_KEEP_RESULT,
            TestBasic.TEST_ABORTED_WITH_ERROR -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_completed_abort))
            TestBasic.BLOCK_COMPLETED -> showAlert(activity, resources.getString(R.string.onend_test), resources.getString(R.string.test_partially_completed))
        }
    }
    
    private fun showNoNetworkDialog(result: TestResult, canTryEmail: Boolean) {
        if (canTryEmail) {
            show2ChoisesDialog(
                activity,
                "No Network Connection",
                "Cannot upload to web backend. Would you like to send results via email instead?",
                "Send Email",
                "Save Locally",
                {
                    // User chose email
                    if (result.code == TestBasic.TEST_COMPLETED) {
                        sendResult(result)
                    } else {
                        askWhetherSending(result)
                    }
                },
                {
                    // User chose to save locally
                    showAlert(activity, "Results Saved", "Results saved locally. Will retry upload when network is available.")
                }
            )
        } else {
            showAlert(
                activity,
                "No Network Connection", 
                "Cannot upload to web backend and email is not configured. Results saved locally."
            )
        }
    }
    
    private fun showDeviceRegistrationRequiredDialog(result: TestResult) {
        show2ChoisesDialog(
            activity,
            "Device Not Registered",
            "This device is not registered. Web upload requires device registration. Would you like to register now?",
            "Register Device",
            "Skip",
            {
                // User chose to register - show registration dialog
                // Note: This would need integration with MainActivity to show the registration dialog
                showAlert(activity, "Registration", "Please register your device from the main menu, then try uploading again.")
            },
            {
                // User chose to skip
                showAlert(activity, "Results Saved", "Results saved locally. Register your device to enable web upload.")
            }
        )
    }

    // Web upload methods
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

                val experimentData = parseExperimentFiles(result)
                if (experimentData != null) {
                    // Ensure device ID is included in experiment data
                    if (deviceManager.isDeviceRegistered) {
                        experimentData.deviceId = deviceManager.deviceId!!
                    }
                    
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
                android.util.Log.e("ResultsManager", "Upload error", e)
            }
        }
    }

    private fun parseExperimentFiles(result: TestResult): ExperimentUploadData? {
        return try {
            val jsonFile = File(result.res_files[0]) // Assuming first file is JSON
            val resultFile = File(result.res_files[1]) // Assuming second file is results
            parseExperimentFiles(jsonFile, resultFile)
        } catch (e: Exception) {
            android.util.Log.e("ResultsManager", "Error parsing experiment files", e)
            null
        }
    }

    private fun parseExperimentFiles(jsonFile: File, resultFile: File): ExperimentUploadData? {
        return try {
            // Parse JSON configuration
            val jsonContent = jsonFile.readText()
            val configJson = JSONObject(jsonContent)
            
            // Generate unique ID if not present
            val uniqueId = configJson.optString("uniqueId").ifEmpty { 
                "${System.currentTimeMillis()}_${UUID.randomUUID().toString().substring(0, 8)}"
            }
            
            // Extract test class name
            val classesArray = configJson.getJSONArray("classes")
            val testClassName = if (classesArray.length() > 0) classesArray.getString(0) else ""
            
            // Parse trial results
            val trials = parseTrialResults(resultFile)
            
            ExperimentUploadData(
                uniqueId = uniqueId,
                testClassName = testClassName,
                configuration = configJson,
                trials = trials
            )
        } catch (e: Exception) {
            android.util.Log.e("ResultsManager", "Error parsing experiment files", e)
            null
        }
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
            android.util.Log.e("ResultsManager", "Error parsing trial results", e)
        }
        
        return trials
    }

    private suspend fun doUploadExperiment(experimentData: ExperimentUploadData): Boolean = withContext(Dispatchers.IO) {
        var attempt = 0
        var delay = retryDelayMs
        
        while (attempt < maxRetryAttempts) {
            try {
                if (!isNetworkAvailable()) {
                    android.util.Log.w("ResultsManager", "No network available for upload attempt ${attempt + 1}")
                    delay(delay)
                    delay = min(delay * 2, 60000) // Exponential backoff, max 1 minute
                    attempt++
                    continue
                }
                
                val url = URL("$webApiUrl/upload/experiment")
                val connection = url.openConnection() as HttpURLConnection
                
                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Accept", "application/json")
                if (webApiKey.isNotEmpty()) {
                    connection.setRequestProperty("Authorization", "Bearer $webApiKey")
                }
                connection.doOutput = true
                connection.connectTimeout = 30000
                connection.readTimeout = 60000
                
                // Create JSON payload with device ID
                val payload = JSONObject().apply {
                    put("unique_id", experimentData.uniqueId)
                    put("test_class_name", experimentData.testClassName)
                    put("device_id", experimentData.deviceId) // Include device ID
                    put("configuration", experimentData.configuration)
                    put("trials", JSONArray().apply {
                        experimentData.trials.forEach { trial ->
                            put(JSONObject().apply {
                                put("trial_number", trial.trialNumber)
                                trial.data.forEach { (key, value) ->
                                    put(key, value)
                                }
                            })
                        }
                    })
                }
                
                // Send request
                connection.outputStream.use { os ->
                    os.write(payload.toString().toByteArray())
                }
                
                val responseCode = connection.responseCode
                
                if (responseCode == HttpURLConnection.HTTP_CREATED) {
                    android.util.Log.i("ResultsManager", "Experiment uploaded successfully")
                    return@withContext true
                } else if (responseCode == HttpURLConnection.HTTP_CONFLICT) {
                    // Duplicate experiment - consider it successful
                    android.util.Log.i("ResultsManager", "Experiment already exists on server")
                    return@withContext true
                } else {
                    val errorMessage = try {
                        connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                    } catch (e: Exception) {
                        "HTTP $responseCode"
                    }
                    android.util.Log.w("ResultsManager", "Upload failed with code $responseCode: $errorMessage")
                }
                
            } catch (e: IOException) {
                android.util.Log.w("ResultsManager", "Upload attempt ${attempt + 1} failed", e)
            } catch (e: Exception) {
                android.util.Log.e("ResultsManager", "Unexpected error during upload", e)
                return@withContext false
            }
            
            attempt++
            if (attempt < maxRetryAttempts) {
                delay(delay)
                delay = min(delay * 2, 60000) // Exponential backoff
            }
        }
        
        android.util.Log.e("ResultsManager", "Upload failed after $maxRetryAttempts attempts")
        return@withContext false
    }

    private fun sendResult(result: TestResult) {
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
                val res = doSendResult(result)
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

    private suspend fun doSendResult(res: TestResult):Boolean = withContext(Dispatchers.IO) {
        val mail = Mail(emailAccount)
        return@withContext  mail.send(emailRecipients, res.mailsubject, res.mailbody, res.res_files)
    }

    private fun moveFilesToPrivateStorage(filePaths: Array<String>) {
        try {
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
                    android.util.Log.i("ResultsManager", "Moved ${sourceFile.name} to private storage")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("ResultsManager", "Error moving files to private storage", e)
        }
    }

    // Data classes for upload
    data class ExperimentUploadData(
        val uniqueId: String,
        val testClassName: String,
        val configuration: JSONObject,
        val trials: List<TrialData>,
        var deviceId: String = "" // Device identifier for tracking
    )

    data class TrialData(
        val trialNumber: Int,
        val data: Map<String, Any>
    )
}

    // Existing email methods remain unchanged