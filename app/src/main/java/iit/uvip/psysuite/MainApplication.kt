package iit.uvip.psysuite


import android.app.Application
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import iit.uvip.psysuite.core.model.preferences.ProjectPreferences
import iit.uvip.psysuite.core.stimuli.DelaysAligner

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it manage SharedPreference and DelaysAligner
//
// if pref file does not exist, fill it with values taken from corresponding resources
// I need delaysAligner to be :
// - Parcelable (thus must have to-be-parceled properties in the costructor)
// - a static property of MainApplication to be accessed


class MainApplication : Application(){

    companion object {
        @JvmStatic var delaysAligner = DelaysAligner()
//        @JvmStatic var delaysAligner = DelaysAligner(10L, 29L, 18L,0L, 0L, 29L, 29L)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ME", "${NavHostFragment::class.java}")

        // create preference file (if not exist), init preferences
        ProjectPreferences.init(applicationContext)
        delaysAligner = ProjectPreferences.createDelaysObject()
    }
}