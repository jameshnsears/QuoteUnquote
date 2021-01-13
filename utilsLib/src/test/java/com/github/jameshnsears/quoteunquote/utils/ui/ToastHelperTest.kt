package com.github.jameshnsears.quoteunquote.utils.ui

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class ToastHelperTest {
    @Test
    fun `demonstrate invocation`() {
        val context: Context = getApplicationContext()
        ToastHelper.makeToast(context, "message1", Toast.LENGTH_LONG)
        ToastHelper.makeToast(context, "message2", Toast.LENGTH_SHORT)
    }
}
