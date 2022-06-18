package com.github.jameshnsears.quoteunquote.configure.fragment.sync

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree
import org.junit.Assert.assertTrue
import org.junit.Test
import timber.log.Timber
import java.io.BufferedReader

class SyncJsonSchemaValidationTest {
    @Test
    fun validateGoodJsonAgainstSchema() {
        if (Timber.treeCount == 0) {
            Timber.plant(MethodLineLoggingTree())
        }

        val inputStream =
            InstrumentationRegistry.getInstrumentation().context.resources.assets
                .open(
                    "restore_one_widget.json"
                )
        val jsonString = inputStream.bufferedReader().use(BufferedReader::readText)

        // be aware of QuoteUnquote/.gradle folder caching old version
        // https://stackoverflow.com/questions/60878947/android-studio-how-to-clear-assets-folder-cache
        assertTrue(
            SyncJsonSchemaValidation.isJsonValid(
                getApplicationContext(), jsonString
            )
        )
    }
}
