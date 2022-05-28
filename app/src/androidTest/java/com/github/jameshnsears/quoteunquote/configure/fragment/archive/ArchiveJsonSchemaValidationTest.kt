package com.github.jameshnsears.quoteunquote.configure.fragment.archive

import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.platform.app.InstrumentationRegistry
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree
import org.junit.Assert.assertTrue
import org.junit.Test
import timber.log.Timber
import java.io.BufferedReader

class ArchiveJsonSchemaValidationTest {
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

        assertTrue(
            ArchiveJsonSchemaValidation.isJsonValid(
                getApplicationContext(), jsonString
            )
        )
    }
}
