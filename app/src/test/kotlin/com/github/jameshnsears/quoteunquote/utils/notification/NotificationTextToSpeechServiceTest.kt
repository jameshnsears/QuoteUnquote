package com.github.jameshnsears.quoteunquote.utils.notification

import android.app.Application
import android.content.Intent
import android.os.Build
import android.speech.tts.TextToSpeech
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class NotificationTextToSpeechServiceTest : ShadowLoggingHelper() {
    @Before
    fun setUp() {
        NotificationTextToSpeechService.isRunning = false
    }

    @Test
    fun serviceLifecycle() {
        val controller = Robolectric.buildService(NotificationTextToSpeechService::class.java)

        assertThat(NotificationTextToSpeechService.isRunning, equalTo(false))

        controller.create()
        assertThat(NotificationTextToSpeechService.isRunning, equalTo(true))

        controller.destroy()
        assertThat(NotificationTextToSpeechService.isRunning, equalTo(false))
    }

    @Test
    fun speakText_excludeSource() {
        val controller = Robolectric.buildService(NotificationTextToSpeechService::class.java)
        controller.create()
        val service = controller.get()

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), NotificationTextToSpeechService::class.java).apply {
                putExtra("textToSpeak", "Hello World")
                putExtra("localeTts", "UK")
                putExtra("excludeSource", true)
            }

        val isTtsInitializedField = NotificationTextToSpeechService::class.java.getDeclaredField("isTtsInitialized")
        isTtsInitializedField.isAccessible = true
        isTtsInitializedField.set(service, true)

        service.onStartCommand(intent, 0, 1)
        ShadowLooper.idleMainLooper()

        val ttsField = NotificationTextToSpeechService::class.java.getDeclaredField("tts")
        ttsField.isAccessible = true
        val tts = ttsField.get(service) as TextToSpeech
        val shadowTts = shadowOf(tts)

        assertThat(shadowTts.lastSpokenText, equalTo("Hello World"))
        assertThat(NotificationTextToSpeechService.localeTts, equalTo(Locale.UK))

        controller.destroy()
    }

    @Test
    fun speakText_includeSource() {
        val controller = Robolectric.buildService(NotificationTextToSpeechService::class.java)
        controller.create()
        val service = controller.get()

        val intent =
            Intent(ApplicationProvider.getApplicationContext(), NotificationTextToSpeechService::class.java).apply {
                putExtra("textToSpeak", "The quote")
                putExtra("textToSpeakSource", "The author")
                putExtra("localeTts", "DEFAULT")
                putExtra("excludeSource", false)
            }

        val isTtsInitializedField = NotificationTextToSpeechService::class.java.getDeclaredField("isTtsInitialized")
        isTtsInitializedField.isAccessible = true
        isTtsInitializedField.set(service, true)

        service.onStartCommand(intent, 0, 1)
        ShadowLooper.idleMainLooper()

        val ttsField = NotificationTextToSpeechService::class.java.getDeclaredField("tts")
        ttsField.isAccessible = true
        val tts = ttsField.get(service) as TextToSpeech
        val shadowTts = shadowOf(tts)

        assertThat(shadowTts.lastSpokenText, equalTo("The quote The author"))
        assertThat(NotificationTextToSpeechService.localeTts, equalTo(Locale.getDefault()))

        controller.destroy()
    }

    @Test
    fun onInit_failure() {
        val controller = Robolectric.buildService(NotificationTextToSpeechService::class.java)
        controller.create()
        val service = controller.get()

        // onInit with failure should clear queue and potentially stopSelf
        service.onInit(TextToSpeech.ERROR)
        ShadowLooper.idleMainLooper()

        // Check if it's still running
        assertThat(NotificationTextToSpeechService.isRunning, equalTo(true))

        controller.destroy()
        assertThat(NotificationTextToSpeechService.isRunning, equalTo(false))
    }
}
