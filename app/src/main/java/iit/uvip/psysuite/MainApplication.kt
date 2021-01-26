package iit.uvip.psysuite


import android.app.Application
import android.os.Build
import iit.uvip.psysuite.core.model.preferences.ProjectPreferences
import iit.uvip.psysuite.core.stimuli.DelaysAligner

// this is called before any activity onCreate

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it manage SharedPreference and DelaysAligner
//
// if pref file does not exist  => fill it with values taken from defaultDelays (devicesDelays["${Build.MODEL}_${Build.VERSION.RELEASE}"]),
// otherwise                    =>
// I need delaysAligner to be :
// - Parcelable (thus must have to-be-parceled properties in the costructor)
// - a static property of MainApplication to be accessed by everywhere


class MainApplication : Application(){


    private var devicesDelays:HashMap<String, DelaysAligner> = hashMapOf(
        "Mi A2 Lite_9" to DelaysAligner(10L, 37L, 0L, 15L, 15L, 35L, 45L),
        "Mi A2 Lite_10" to DelaysAligner(10L, 37L, 0L, 15L, 15L, 35L, 45L),
        "SM-A405FN_9" to DelaysAligner(10L, 36L, 0L, 38L, 38L, 25L, 35L),
        "SM-A405FN_10" to DelaysAligner(10L, 36L, 0L, 38L, 38L, 25L, 35L)
    )

    private val defaultDelays:DelaysAligner = devicesDelays["${Build.MODEL}_${Build.VERSION.RELEASE}"] ?: devicesDelays["SM-A405FN_10"]!!

    companion object {
        @JvmStatic var delaysAligner = DelaysAligner()
    }

    override fun onCreate() {
        super.onCreate()

        // create preference file (if not exist), init preferences
        ProjectPreferences.init(applicationContext, defaultDelays)
        delaysAligner = ProjectPreferences.getSystemDelays()
    }
}