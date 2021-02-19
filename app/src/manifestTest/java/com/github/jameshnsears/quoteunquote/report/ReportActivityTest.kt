package com.github.jameshnsears.quoteunquote.report

import android.os.Build
import android.widget.Spinner
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.launchActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.ConcurrentHashMap

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ReportActivityTest : ShadowLoggingHelper() {
    lateinit var scenario: ActivityScenario<ReportActivityDouble>

    @Before
    fun before() {
        scenario = launchActivity(ReportActivityDouble.getIntent())
    }

    @After
    fun after() {
        scenario.close()
    }

    @Test
    fun hasQuotationAlreadyBeenReported() {
        scenario.onActivity { activity ->
            assertFalse(activity.hasQuotationAlreadyBeenReported())
        }
    }

    @Test
    fun itemsInReasonSpinner() {
        scenario.onActivity { activity ->
            val spinnerReason: Spinner = activity.findViewById(R.id.spinnerReason)
            assertThat("", spinnerReason.adapter.count, Is.`is`(6))
        }
    }

    @Test
    fun auditProperties() {
        scenario.onActivity { activity ->
            val expectedConcurrentHashMap: ConcurrentHashMap<String, String> = ConcurrentHashMap()
            expectedConcurrentHashMap["Report"] = "digest=d; author=a; reason=Attribution; notes="
            assertTrue(expectedConcurrentHashMap == activity.auditProperties)
        }
    }
}
