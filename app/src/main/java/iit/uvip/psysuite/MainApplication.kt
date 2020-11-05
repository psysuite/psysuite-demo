package iit.uvip.psysuite


import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.util.Log
import androidx.navigation.fragment.NavHostFragment
import iit.uvip.psysuite.core.stimuli.DelaysAligner

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it contains support for:
// - tts

class MainApplication : Application(){



    companion object {
        @JvmStatic val delaysAligner = DelaysAligner(10L, 29L, 18L,0L, 0L, 29L, 29L)
//        @JvmStatic val delaysAligner = DelaysAligner(0L, 0L, 6L, 6L, 35L, 35L)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("ME", "${NavHostFragment::class.java}")

//        val hasLowLatencyFeature: Boolean = packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)
//        val hasProFeature: Boolean = packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)

        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val sampleRateStr: String? = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
        var sampleRate: Int = sampleRateStr?.let { str ->
            Integer.parseInt(str).takeUnless { it == 0 }
        } ?: 44100 // Use a default value if property not found

        val framesPerBuffer: String? = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
        var framesPerBufferInt: Int = framesPerBuffer?.let { str ->
            Integer.parseInt(str).takeUnless { it == 0 }
        } ?: 256 // Use default



    }

}