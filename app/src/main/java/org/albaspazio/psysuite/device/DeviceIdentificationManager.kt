package org.albaspazio.psysuite.device

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class DeviceIdentificationManager private constructor(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "psysuite_device_config"
        private const val KEY_DEVICE_ID = "device_identifier"
        private const val KEY_REGISTRATION_DATE = "registration_date"
        private const val KEY_IS_REGISTERED = "is_registered"
        private const val KEY_REGISTRATION_SKIPPED = "registration_skipped"
        private const val KEY_FIRST_LAUNCH_HANDLED = "first_launch_handled"
        
        @Volatile
        private var INSTANCE: DeviceIdentificationManager? = null
        
        fun getInstance(context: Context): DeviceIdentificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeviceIdentificationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    val isDeviceRegistered: Boolean
        get() = sharedPrefs.getBoolean(KEY_IS_REGISTERED, false) && !deviceId.isNullOrEmpty()

    val isFirstLaunch: Boolean
        get() = !sharedPrefs.getBoolean(KEY_FIRST_LAUNCH_HANDLED, false)

    val isRegistrationSkipped: Boolean
        get() = sharedPrefs.getBoolean(KEY_REGISTRATION_SKIPPED, false)

    val getRegistrationDate: Long
        get() = sharedPrefs.getLong(KEY_REGISTRATION_DATE, 0)

    val deviceId: String?
        get() = sharedPrefs.getString(KEY_DEVICE_ID, null)

    /*
     * Set the device ID and mark it as registered in shared preferences.
     */
    fun setDeviceId(deviceId: String): Boolean {
        if (deviceId.isBlank()) return false
        
        return sharedPrefs.edit()
            .putString(KEY_DEVICE_ID, deviceId.trim())
            .putBoolean(KEY_IS_REGISTERED, true)
            .putBoolean(KEY_FIRST_LAUNCH_HANDLED, true)
            .putBoolean(KEY_REGISTRATION_SKIPPED, false) // Clear skip flag if user registers
            .putLong(KEY_REGISTRATION_DATE, System.currentTimeMillis())
            .commit()
    }
    
    fun skipRegistration() {
        sharedPrefs.edit {
            putBoolean(KEY_REGISTRATION_SKIPPED, true)
            .putBoolean(KEY_FIRST_LAUNCH_HANDLED, true)
            .putBoolean(KEY_IS_REGISTERED, false)
        }
    }

    fun clearRegistration() {
        sharedPrefs.edit {
            remove(KEY_DEVICE_ID)
            .putBoolean(KEY_IS_REGISTERED, false)
            .putBoolean(KEY_REGISTRATION_SKIPPED, false)
            .remove(KEY_REGISTRATION_DATE)
        }
    }
    
    val registrationStatus: String
        get() =  when {
                    isDeviceRegistered      -> "Registered: ${deviceId}"
                    isRegistrationSkipped   -> "Registration skipped"
                    else                    -> "Not registered"
                 }

    
    fun validateDeviceId(deviceId: String): Boolean {
        return deviceId.length >= 3 && 
               deviceId.length <= 50 && 
               deviceId.matches(Regex("[a-zA-Z0-9\\-_]+"))
    }
}