package com.github.jameshnsears.quoteunquote.utils.audit

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.ConcurrentHashMap

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AuditEventHelperTest {
    @Test
    fun `demonstrate invocation`() {
        AuditEventHelper.createInstance(getApplicationContext())
        val properties: ConcurrentHashMap<String, String> = ConcurrentHashMap()
        AuditEventHelper.auditEvent("", properties)
    }
}
