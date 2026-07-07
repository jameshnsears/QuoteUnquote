package com.github.jameshnsears.quoteunquote.configure.fragment

import android.app.Application
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class QuoteUnquoteColorPickerDialogTest {
    @Test
    fun builderBasicSetup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.setTheme(R.style.AppTheme)
        val builder = QuoteUnquoteColorPickerDialog.Builder(context)

        assertThat(builder.colorPickerView, notNullValue())

        builder.setPreferenceName("test_preference")
        assertThat(builder.colorPickerView.preferenceName, equalTo("test_preference"))

        builder.attachAlphaSlideBar(true)
        builder.attachBrightnessSlideBar(false)

        builder.setPositiveButton("OK") { _, _ ->
            // do nothing
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        assertThat(dialog, notNullValue())
    }

    @Test
    fun builderPositiveButtonWithListener() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.setTheme(R.style.AppTheme)
        val builder = QuoteUnquoteColorPickerDialog.Builder(context)

        builder.setPositiveButton(
            "OK",
            object : ColorEnvelopeListener {
                override fun onColorSelected(
                    envelope: ColorEnvelope?,
                    fromUser: Boolean,
                ) {
                    // ignore
                }
            },
        )

        val dialog = builder.create()
        assertThat(dialog, notNullValue())
    }

    @Test
    fun builderWithTheme() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val builder = QuoteUnquoteColorPickerDialog.Builder(context, R.style.AppTheme)
        val dialog = builder.create()
        assertThat(dialog, notNullValue())
    }

    @Test
    fun testSetColorPickerView() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.setTheme(R.style.AppTheme)
        val builder = QuoteUnquoteColorPickerDialog.Builder(context)

        val customColorPickerView = ColorPickerView(context)
        builder.setColorPickerView(customColorPickerView)

        assertThat(builder.colorPickerView, equalTo(customColorPickerView))
    }

    @Test
    fun testBottomSpace() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        context.setTheme(R.style.AppTheme)
        val builder = QuoteUnquoteColorPickerDialog.Builder(context)

        builder.setBottomSpace(20)
        val dialog = builder.create()
        assertThat(dialog, notNullValue())
    }
}
