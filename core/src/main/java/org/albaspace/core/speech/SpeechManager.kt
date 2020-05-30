package org.albaspace.core.speech

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.os.Handler
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import org.albaspace.core.R
import java.util.*


class SpeechManager(
    private val resources: Resources,
    applicationContext: Context,
    private var pause: Int = PAUSE
) {

    companion object {
        private const val PAUSE: Int = 500
    }

    private val utterance_silence: String = "SILENCE"
    private val utterance_text: String = "TEXT"


    private var tts: TextToSpeech? = null
    private var textList: MutableList<String> = mutableListOf()
    var isValid: Boolean = false
    private var isSpeaking: Boolean = false
    private var mSpeakHandler: Handler = Handler()

    var callback: (Unit) -> Unit = {}

    // init tts, load Language & set progress listener
    init {
        tts = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {

                val result = tts!!.setLanguage(Locale.ITALIAN)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", resources.getString(R.string.warn_tts_language_notsupported))
                }
                isValid = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {

                    override fun onStart(utterance_id: String?) {
                        //Log.d("TTS","START TTS")
                        isSpeaking = true
                    }

                    override fun onStop(utterance_id: String?, interrupted: Boolean) {
                        super.onStop(utterance_id, interrupted)
                        //Log.d("TTS","STOP")
                        isSpeaking = false
                    }

                    override fun onDone(utterance_id: String?) {
                        isSpeaking = false
                        //Log.d("TTS","DONE")

                        if (textList.size > 0)
                            when (utterance_id) {
                                utterance_text -> {
                                    if (textList.size > 0) tts?.playSilentUtterance(
                                        pause.toLong(),
                                        TextToSpeech.QUEUE_ADD,
                                        utterance_silence
                                    )
                                }
                                utterance_silence -> doSpeak()
                            }
                        else {
                            if (callback != {}) {
                                callback(Unit)
                                callback = {}
                            }
                        }
                    }

                    override fun onError(utterance_id: String?) {
                        Log.d("TTS", "ERROR")
                    }
                })
            } else {
                Log.e("TTS", resources.getString(R.string.error_tts_notinitialized))
                tts = null
                isValid = false
            }
        })
    }

    // does the work !
    private fun doSpeak(
        queue_mode: Int = TextToSpeech.QUEUE_ADD,
        params: Bundle? = null,
        utterance_id: String = utterance_text
    ) {
        val text = textList.removeAt(0)
        Log.i("TTS", "--------------->: $text")
        tts?.speak(text, queue_mode, params, utterance_id)
    }

    fun speak(
        text: String,
        queue_mode: Int = TextToSpeech.QUEUE_ADD,
        params: Bundle? = null,
        utterance_id: String = utterance_text,
        clb: (Unit) -> Unit = {},
        delay: Long = 0
    ) {
        speak(listOf(text), queue_mode, params, utterance_id, pause, clb, delay)
    }

    fun speak(
        text: List<String>,
        queue_mode: Int = TextToSpeech.QUEUE_ADD,
        params: Bundle? = null,
        utterance_id: String = utterance_text,
        pause: Int = PAUSE,
        clb: (Unit) -> Unit = {},
        delay: Long = 0
    ) {
        callback = clb
        this.pause = pause
        textList.addAll(text)
        if (!isSpeaking) {
            if (delay > 0) mSpeakHandler.postDelayed(
                { doSpeak(queue_mode, params, utterance_id) },
                delay
            )
            else doSpeak(queue_mode, params, utterance_id)
        }
    }

    fun stopAndSpeak(
        text: String,
        queue_mode: Int = TextToSpeech.QUEUE_ADD,
        params: Bundle? = null,
        utterance_id: String = utterance_text,
        clb: (Unit) -> Unit = {},
        delay: Long = 0
    ) {
        stopAndSpeak(listOf(text), queue_mode, params, utterance_id, pause, clb, delay)
    }

    fun stopAndSpeak(
        text: List<String>,
        queue_mode: Int = TextToSpeech.QUEUE_ADD,
        params: Bundle? = null,
        utterance_id: String = utterance_text,
        pause: Int = PAUSE,
        clb: (Unit) -> Unit = {},
        delay: Long = 0
    ) {
        stop()  // also set isSpeaking=false
        textList.clear()
        isSpeaking = false
        speak(text, queue_mode, params, utterance_id, pause, clb, delay)
    }

    fun stop() {
        mSpeakHandler.removeCallbacksAndMessages(null)
        textList.clear()
        tts?.stop()
        isSpeaking =
            false  // i cannot wait that onStop set it to false, otherwise if I give a speak command immediately after this stop, it doesn't start
    }

    fun shutdown() {
        if (isValid) tts?.shutdown()
    }
}