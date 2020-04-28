package iit.uvip.audiotactilebindingapp


import android.app.Application
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import java.util.*

// HERE SHOULD GO ONLY IMMUTABLE DATA !!!! (since system may re-create this instance silently)
// now it contains support for:
// - tts
// - vibrator

class MainApplication : Application(), TextToSpeech.OnInitListener  {

    lateinit var vibrator: Vibrator
    var tts: TextToSpeech? = null

    companion object {

        @JvmStatic val FILE_EXTENSION: String = ".json"
        @JvmStatic val RES_EXTENSION: String = ".txt"
    }

    override fun onCreate() {
        super.onCreate()

        Log.d("ME", "${NavHostFragment::class.java}")

        vibrator    = getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
        tts         = TextToSpeech(applicationContext, this)
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.ITALIAN)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "The Language specified is not supported!")
            }

        } else {
            Log.e("TTS", "Initialization Failed!")
            tts = null
        }
    }
}