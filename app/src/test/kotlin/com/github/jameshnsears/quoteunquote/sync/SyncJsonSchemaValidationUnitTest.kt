package com.github.jameshnsears.quoteunquote.sync

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class SyncJsonSchemaValidationUnitTest {
    @Test
    fun validateBackupJsonAgainstSchema() {
        val context = ApplicationProvider.getApplicationContext<Application>()

        val json =
            this.javaClass.classLoader!!
                .getResourceAsStream("InternalDatabase/PIDsdD6B16.json")
                .bufferedReader()
                .use { it.readText() }

        assertThat(
            SyncJsonSchemaValidation.isJsonValid(context, json),
            `is`(true),
        )
    }
}
