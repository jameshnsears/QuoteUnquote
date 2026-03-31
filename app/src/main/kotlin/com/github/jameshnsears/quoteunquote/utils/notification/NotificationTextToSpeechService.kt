package com.github.jameshnsears.quoteunquote.utils.notification

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.ConcurrentLinkedQueue

class NotificationTextToSpeechService : Service(), TextToSpeech.OnInitListener {
    companion object {
        var isRunning = false
        var localeTts = Locale.UK
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val textQueue = ConcurrentLinkedQueue<String>()

    override fun onCreate() {
        super.onCreate()
        isRunning = true // Mark service as running
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        localeTts = when (intent?.getStringExtra("localeTts") ?: "UK") {
            "UK" -> Locale.UK
            else -> Locale.getDefault()
        }

        var textToSpeak = intent?.getStringExtra("textToSpeak") ?: ""
        if (intent?.getBooleanExtra("excludeSource", false) == false) {
            val source = intent.getStringExtra("textToSpeakSource") ?: ""
            textToSpeak += " $source"
        }

        if (textToSpeak.isNotBlank()) {
            textQueue.add(textToSpeak)
            processTextQueue()
        }

        return START_NOT_STICKY
    }

    private fun processTextQueue() {
        if (isTtsInitialized && tts != null && textQueue.isNotEmpty()) {
            while (textQueue.isNotEmpty()) {
                val text = textQueue.poll() ?: break
                tts?.speak(
                    text,
                    TextToSpeech.QUEUE_ADD,
                    null,
                    "utteranceId-${System.currentTimeMillis()}",
                )
            }
            // Optional: stopSelf() here if you want the service to die after speaking
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(localeTts)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.e("TTS: init fail: language not supported")
                cleanupAndStop()
            } else {
                isTtsInitialized = true
                Timber.d("TTS: init success")
                processTextQueue()
            }
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
