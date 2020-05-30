package org.albaspace.core.speech

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import org.albaspace.core.R
import org.albaspace.core.accessory.Optional
import org.albaspace.core.accessory.isPresent
import org.albaspace.core.accessory.orNull
import org.albaspace.core.accessory.toOptional
import java.util.*

class SpeechRecognitionManager(private val ctx: Context) {

    private var speechRecognizer: SpeechRecognizer  = SpeechRecognizer.createSpeechRecognizer(ctx)
    private val speechRelay                         = PublishRelay.create<Optional<Pair<Int, String?>>>()

    private var isRecognizing:Boolean = false

    companion object {
        @JvmStatic val REC_SUCCESS = 0
        @JvmStatic val EMPTY_SPEECH = 10
    }

    fun getSpeechInput(): Maybe<Pair<Int, String?>> {

        return speechRelay
            .firstOrError()
            .filter { it.isPresent() }
            .map { it.orNull()!! }
            .doOnSubscribe {
                // Intent to listen to user vocal input and return the result to the same activity.
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)   // Use a language model based on free-form speech recognition.
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.packageName)
                }
                val listener = CustomRecognitionListener(ctx.resources) {
                    val res = it.orNull()

                    if (!res?.second.isNullOrBlank())   speechRelay.accept(it)
                    else                                speechRelay.accept(Pair(EMPTY_SPEECH, ctx.resources.getString(R.string.char_recognition_empty)).toOptional())

                    speechRecognizer.cancel()   // issue when I call stop() from outside. without this cancel its gives me error8 next recognition
                                                // this line is not necessary when stop() is not called
                }
                speechRecognizer.setRecognitionListener(listener)
                speechRecognizer.startListening(intent)
                isRecognizing = true
            }
            .subscribeOn(AndroidSchedulers.mainThread())        // affects upward lambda
            .cache()
    }

    fun stop(){
        speechRelay.accept(Pair(11, "").toOptional())   // raising this event make the stop more stable,
                                                        // otherwise, sometimes a error #8 appears on next recognition
        speechRecognizer.cancel()
    }

    inner class CustomRecognitionListener(
        private val resources: Resources,
        private val clb: (Optional<Pair<Int, String?>>) -> Unit
    ) :
        RecognitionListener {

            override fun onResults(results: Bundle) {
                val result: String? = results.getStringArrayList("results_recognition")?.firstOrNull()
                clb(Pair(REC_SUCCESS, result).toOptional())
            }

            private val TAG = "RecognitionListener"

            override fun onError(error: Int) {

                val error_string = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH                 -> resources.getString(R.string.char_recognition_empty)
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT           -> resources.getString(R.string.char_recognition_timeout_error)
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> resources.getString(R.string.char_recognition_missing_permission)
                    SpeechRecognizer.ERROR_NETWORK                  -> resources.getString(R.string.char_recognition_network_error)
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY          -> resources.getString(R.string.char_recognition_busy_error)
                    else                                            -> resources.getString(R.string.char_recognition_generic_error)
                }
                Log.e(TAG, "error $error : $error_string")

                clb(Pair(error, error_string).toOptional())
            }

            override fun onPartialResults(partialResults: Bundle)   {/*Log.d(TAG, "onPartialResults")*/}
            override fun onEvent(eventType: Int, params: Bundle)    {/*Log.d(TAG, "onEvent $eventType")*/}
            override fun onReadyForSpeech(params: Bundle)           {/*Log.d(TAG, "onReadyForSpeech")*/}
            override fun onBeginningOfSpeech()                      {/*Log.d(TAG, "onBeginningOfSpeech")*/}
            override fun onRmsChanged(rmsdB: Float)                 {/*Log.d(TAG, "onRmsChanged")*/}
            override fun onBufferReceived(buffer: ByteArray)        {/*Log.d(TAG, "onBufferReceived")*/}
            override fun onEndOfSpeech()                            {/*Log.d(TAG, "onEndofSpeech")*/}
    }
}

/*
in the calling class write it

    private lateinit var srm:SpeechRecognitionManager = SpeechRecognitionManager(requireContext())

    private fun recognize(valid_results:List<String> = listOf()){
        srm.getSpeechInput()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    when(it.first)
                        SUCCESS ->{
                            if(!it.isNullOrBlank()) {}
                            else                    {
                                // check whether given response is allowed
                                val res:Boolean =   if(valid_results.isEmpty())     true
                                                    else                            valid_results.contains(it.second)
                                if(res){
                                    // text recognized AND allowed
                                }
                                else{
                                    // text recognized but not allowed
                                    recognize(valid_results)        // rec again
                                }

                            }
                        }
                        else -> {   // RECOGNIZER ERROR
                        }
                },
                onError = {
                    Log.e("", it.toString())
                }
            )
            .addTo(disposable)
    }
 */