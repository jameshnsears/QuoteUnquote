package com.github.jameshnsears.quoteunquote.utils.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

class NotificationTextToSpeechService :
    Service(),
    TextToSpeech.OnInitListener {
    companion object {
        var isRunning = false
        var localeTts = Locale.UK
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val textQueue = ConcurrentLinkedQueue<Pair<String, Locale>>()

    override fun onCreate() {
        super.onCreate()
        isRunning = true // Mark service as running
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        localeTts =
            when (intent?.getStringExtra("localeTts") ?: "UK") {
                "UK" -> Locale.UK
                else -> Locale.getDefault()
            }

        var textToSpeak = intent?.getStringExtra("textToSpeak") ?: ""
        if (intent?.getBooleanExtra("excludeSource", false) == false) {
            val source = intent.getStringExtra("textToSpeakSource") ?: ""
            textToSpeak += " $source"
        }

        if (textToSpeak.isNotBlank()) {
            textQueue.add(Pair(textToSpeak, localeTts))
            processTextQueue()
        }

        return START_NOT_STICKY
    }

    private fun processTextQueue() {
        if (isTtsInitialized && tts != null && textQueue.isNotEmpty()) {
            while (textQueue.isNotEmpty()) {
                val (text, locale) = textQueue.poll() ?: break
                val result = tts?.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Timber.e("TTS: language not supported: $locale")
                }

                tts?.speak(
                    text,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "utteranceId-${System.currentTimeMillis()}",
                )
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            Timber.d("TTS: init success")
            processTextQueue()
        } else {
            Timber.e("TTS: init fail")
            cleanupAndStop()
        }
    }

    private fun cleanupAndStop() {
        textQueue.clear()
        stopSelf()
    }

    override fun onDestroy() {
        tts?.stop() // Stop current speech
        tts?.shutdown() // Release engine resources
        tts = null

        isRunning = false
        isTtsInitialized = false
        super.onDestroy()
        Timber.d("TTS: service destroyed and resources cleaned up")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
