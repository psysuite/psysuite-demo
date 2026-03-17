package org.albaspazio.psysuite.device

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import org.albaspazio.psysuite.R
import java.text.SimpleDateFormat
import java.util.*

class DeviceRegistrationDialog : DialogFragment() {
    
    interface OnDeviceRegisteredListener {
        fun onDeviceRegistered(deviceId: String)
        fun onRegistrationSkipped()
        fun onRegistrationCancelled()
    }
    
    private var listener: OnDeviceRegisteredListener? = null
    private lateinit var deviceManager: DeviceIdentificationManager
    private var isFirstLaunch: Boolean = false
    private var allowSkip: Boolean = true
    
    companion object {
        private const val ARG_FIRST_LAUNCH = "first_launch"
        private const val ARG_ALLOW_SKIP = "allow_skip"
        
        fun newInstance(isFirstLaunch: Boolean = false, allowSkip: Boolean = true): DeviceRegistrationDialog {
            val dialog = DeviceRegistrationDialog()
            val args = Bundle().apply {
                putBoolean(ARG_FIRST_LAUNCH, isFirstLaunch)
                putBoolean(ARG_ALLOW_SKIP, allowSkip)
            }
            dialog.arguments = args
            return dialog
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            isFirstLaunch = it.getBoolean(ARG_FIRST_LAUNCH, false)
            allowSkip = it.getBoolean(ARG_ALLOW_SKIP, true)
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        deviceManager = DeviceIdentificationManager.getInstance(requireContext())
        
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_device_registration, null)
        val editTextDeviceId = view.findViewById<EditText>(R.id.editTextDeviceId)
        val textViewInfo = view.findViewById<TextView>(R.id.textViewInfo)
        
        // Pre-populate with current ID or suggested ID
        val currentId = deviceManager.deviceId
        editTextDeviceId.setText(currentId ?: generateSuggestedDeviceId())
        
        val infoText = if (isFirstLaunch) {
            """
            Welcome to PsySuite!
            
            Would you like to register this device with a unique identifier?
            This helps track which device generated test data.
            
            You can skip this step and register later from the menu.
            
            Suggested format: Location-DeviceType-Number
            Examples: Lab1-Tablet-01, Office-Phone-A, Clinic-iPad-02
            """.trimIndent()
        } else {
            """
            Device Registration
            
            Provide a unique identifier for this PsySuite installation.
            This ID will be used to track which device generated test data.
            
            Current status: ${deviceManager.registrationStatus}
            
            Suggested format: Location-DeviceType-Number
            Examples: Lab1-Tablet-01, Office-Phone-A, Clinic-iPad-02
            """.trimIndent()
        }
        
        textViewInfo.text = infoText
        
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(if (isFirstLaunch) "Welcome to PsySuite" else "Device Registration")
            .setView(view)
            .setPositiveButton("Register") { _, _ ->
                val deviceId = editTextDeviceId.text.toString().trim()
                if (deviceManager.validateDeviceId(deviceId)) {
                    deviceManager.setDeviceId(deviceId)
                    listener?.onDeviceRegistered(deviceId)
                } else {
                    showValidationError()
                }
            }
        
        if (allowSkip) {
            builder.setNeutralButton("Skip") { _, _ ->
                if (isFirstLaunch) deviceManager.skipRegistration()

                listener?.onRegistrationSkipped()
            }
        }
        
        builder.setNegativeButton("Cancel") { _, _ ->
            listener?.onRegistrationCancelled()
        }
        
        builder.setCancelable(!isFirstLaunch) // Don't allow cancel on first launch
        
        return builder.create()
    }
    
    private fun generateSuggestedDeviceId(): String {
        val deviceInfo = Build.MODEL.replace(" ", "").replace("[^a-zA-Z0-9]".toRegex(), "")
        val timestamp = SimpleDateFormat("MMdd", Locale.US).format(Date())
        return "PsySuite-$deviceInfo-$timestamp"
    }
    
    private fun showValidationError() {
        Toast.makeText(context, 
            "Device ID must be 3-50 characters, letters, numbers, hyphens and underscores only", 
            Toast.LENGTH_LONG).show()
    }
    
    fun setOnDeviceRegisteredListener(listener: OnDeviceRegisteredListener) {
        this.listener = listener
    }
}