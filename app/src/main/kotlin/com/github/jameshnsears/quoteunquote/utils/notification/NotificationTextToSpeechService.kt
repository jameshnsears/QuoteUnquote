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
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false
    private val textQueue = ConcurrentLinkedQueue<String>()

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        textQueue.add(intent?.getStringExtra("textToSpeak") ?: "")
        processTextQueue()
        return START_NOT_STICKY
    }

    private fun processTextQueue() {
        if (isTtsInitialized && tts != null && textQueue.isNotEmpty()) {
            val text = textQueue.poll() ?: return

            Timber.d("TTS: speak: $text")

            tts?.speak(
                text,
                TextToSpeech.QUEUE_FLUSH,
                null,
                "utteranceId-${System.currentTimeMillis()}",
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsInitialized = true
            Timber.d("TTS: init success")

            val localeDefault = Locale.getDefault()
            val localUK = Locale.UK
            val result = tts?.setLanguage(localUK)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Timber.e("TTS: init fail: language not supported")
            } else {
                processTextQueue()
            }
        } else {
            Timber.e("TTS: init fail")
        }
    }

    override fun onDestroy() {
        tts?.shutdown()

        isRunning = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
