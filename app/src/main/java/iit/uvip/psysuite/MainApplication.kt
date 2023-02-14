package iit.uvip.psysuite


import android.app.Application
import android.os.Build
import android.util.Log
import iit.uvip.psysuite.core.model.preferences.ProjectPreferences
import iit.uvip.psysuite.core.stimuli.DelaysAligner

// this is called before any activity onCreate

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it manage SharedPreference and DelaysAligner
//
// if pref file does not exist  => fill it with values taken from defaultDelays (devicesDelays[Build.MODEL]),
// otherwise                    =>
// I need delaysAligner to be :
// - Parcelable (thus must have to-be-parceled properties in the costructor)
// - a static property of MainApplication to be accessed by everywhere

class MainApplication : Application(){

    private var devicesDelays:HashMap<String, DelaysAligner> = hashMapOf(
        "Mi A2 Lite"  to DelaysAligner(4L,  40L, 4L, 0L, 5L, 5L, 30L, 53L),
        "SM-A405FN"   to DelaysAligner(0L, 165L, 4L, 0L,28L, 28L, 20L,  0L),
    )

    private val defaultDelays:DelaysAligner = devicesDelays[Build.MODEL] ?: devicesDelays["SM-A405FN"]!!

    companion object {
        @JvmStatic var delaysAligner = DelaysAligner()
    }

    override fun onCreate() {
        super.onCreate()

        // create preference file (if not exist), init preferences
        ProjectPreferences.init(applicationContext, defaultDelays)
        delaysAligner = ProjectPreferences.getSystemDelays()
        Log.d("MainApplication", "OnCreate: SYSTEM DELAYS=> $delaysAligner")
    }
}