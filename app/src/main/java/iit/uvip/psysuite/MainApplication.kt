package iit.uvip.psysuite


import android.app.Application
import android.util.Log
import androidx.navigation.fragment.NavHostFragment

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it contains support for:
// - tts

class MainApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        Log.d("ME", "${NavHostFragment::class.java}")
    }

}