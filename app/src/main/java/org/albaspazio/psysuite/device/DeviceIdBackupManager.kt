package org.albaspazio.psysuite.device

import android.content.Context
import android.util.Log
import java.io.File

/*
 * Class to manage device ID backup and restore.
 */
class DeviceIdBackupManager(private val context: Context) {
    
    private val backupFile = File(context.filesDir, ".psysuite_device_backup")
    
    fun backupDeviceId(deviceId: String) {
        try {
            backupFile.writeText(deviceId)
            Log.d("DeviceIdBackup", "Device ID backed up successfully")
        } catch (e: Exception) {
            Log.e("DeviceIdBackup", "Failed to backup device ID", e)
        }
    }
    
    fun restoreDeviceId(): String? {
        return try {
            if (backupFile.exists()) {
                val restoredId = backupFile.readText().trim()
                if (restoredId.isNotEmpty()) {
                    Log.d("DeviceIdBackup", "Device ID restored from backup")
                    restoredId
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("DeviceIdBackup", "Failed to restore device ID", e)
            null
        }
    }
    
    fun clearBackup() {
        try {
            if (backupFile.exists()) {
                backupFile.delete()
                Log.d("DeviceIdBackup", "Device ID backup cleared")
            }
        } catch (e: Exception) {
            Log.e("DeviceIdBackup", "Failed to clear device ID backup", e)
        }
    }
}