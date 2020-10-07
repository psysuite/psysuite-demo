package iit.uvip.psysuite


import android.app.Application
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import iit.uvip.psysuite.core.common.DelaysAligner

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it contains support for:
// - tts

class MainApplication : Application(){


    companion object {
        @JvmStatic val delaysAligner = DelaysAligner(0L, 0L, 6L, 6L, 35L, 35L)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ME", "${NavHostFragment::class.java}")
    }

}