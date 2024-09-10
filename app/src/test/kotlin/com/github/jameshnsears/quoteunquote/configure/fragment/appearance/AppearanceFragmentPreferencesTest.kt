package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.AppearanceStyleFragmentDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.dialog.StyleDialogAuthorDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.toolbar.AppearanceToolbarFragmentDouble
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class AppearanceFragmentPreferencesTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialAppearanceStylePreferences() {
        with(launchFragment<AppearanceStyleFragmentDouble>()) {
            onFragment { fragment ->
                fragment.setBackgroundColour()
                assertEquals("#FFFFFFFF", fragment.appearancePreferences?.appearanceColour)

                fragment.setTransparency()
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(-1))

                fragment.setTextFamily()
                assertEquals(
                    "Sans Serif",
                    fragment.appearancePreferences?.appearanceTextFamily,
                )

                fragment.setTextStyle()
                assertEquals(
                    "Regular",
                    fragment.appearancePreferences?.appearanceTextStyle,
                )

                fragment.setTextForceItalicRegular()
                assertEquals(
                    true,
                    fragment.appearancePreferences?.appearanceTextForceItalicRegular,
                )
            }
        }
    }

    fun confirmInitialTextPreferencesAuthor() {
        with(launchFragment<StyleDialogAuthorDouble>()) {
            onFragment { fragment ->
                fragment.setTextColour()
                assertEquals(
                    "#FF000000",
                    fragment.appearancePreferences?.appearanceAuthorTextColour,
                )

                fragment.setTextSize()
                assertThat(fragment.appearancePreferences?.appearanceAuthorTextSize, equalTo(16))

                fragment.setTextHide()
                assertTrue(fragment.appearancePreferences?.appearanceAuthorTextHide == false)
            }
        }
    }

    @Test
    fun confirmInitialToolbarPreferences() {
        with(launchFragment<AppearanceToolbarFragmentDouble>()) {
            onFragment { fragment ->
                fragment.setToolbarColour()
                assertThat(
                    fragment.appearancePreferences?.appearanceToolbarColour,
                    equalTo("#FF000000"),
                )

                fragment.setToolbar()
                assertTrue(fragment.appearancePreferences?.appearanceToolbarFirst == false)
                assertTrue(fragment.appearancePreferences?.appearanceToolbarPrevious == true)
                assertTrue(fragment.appearancePreferences?.appearanceToolbarFavourite == true)
                assertTrue(fragment.appearancePreferences?.appearanceToolbarShare == false)
                assertTrue(fragment.appearancePreferences?.appearanceToolbarJump == true)
                assertTrue(fragment.appearancePreferences?.appearanceToolbarRandom == true)
                assertTrue(fragment.appearancePreferences?.appearanceToolbarSequential == false)
            }
        }
    }

    @Test
    fun confirmToolbarChangesToPreferences() {
        with(launchFragment<AppearanceToolbarFragmentDouble>()) {
            onFragment { fragment ->
                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchFirst?.isChecked = true
                assertTrue(fragment.appearancePreferences?.appearanceToolbarFirst == true)

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchPrevious?.isChecked =
                    false
                assertTrue(fragment.appearancePreferences?.appearanceToolbarPrevious == false)

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchToggleFavourite?.isChecked =
                    false
                assertTrue(fragment.appearancePreferences?.appearanceToolbarFavourite == false)

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchShare?.isChecked = false
                assertTrue(fragment.appearancePreferences?.appearanceToolbarShare == false)

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchNextRandom?.isChecked =
                    false
                assertTrue(fragment.appearancePreferences?.appearanceToolbarRandom == false)

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchNextSequential?.isChecked =
                    true
                assertTrue(fragment.appearancePreferences?.appearanceToolbarSequential == true)
            }
        }
    }

    @Test
    fun emptyDeletedStylePreferences() {
        with(launchFragment<AppearanceStyleFragmentDouble>()) {
            onFragment { fragment ->
                fragment.appearancePreferences?.appearanceTransparency = 5
                fragment.setTransparency()
                assertEquals(5, fragment.appearancePreferences?.appearanceTransparency)

                AppearancePreferences.delete(getApplicationContext(), WidgetIdHelper.WIDGET_ID_02)
                assertEquals(5, fragment.appearancePreferences?.appearanceTransparency)

                AppearancePreferences.delete(getApplicationContext(), WidgetIdHelper.WIDGET_ID_01)
                assertEquals(-1, fragment.appearancePreferences?.appearanceTransparency)
            }
        }
    }

    @Test
    fun emptyDisabledPreferences() {
        with(launchFragment<AppearanceStyleFragmentDouble>()) {
            onFragment { fragment ->
                fragment.appearancePreferences?.appearanceTransparency = 5
                fragment.setTransparency()
                assertEquals(5, fragment.appearancePreferences?.appearanceTransparency)

                AppearancePreferences.erase(getApplicationContext())
                assertEquals(-1, fragment.appearancePreferences?.appearanceTransparency)
            }
        }
    }
}
