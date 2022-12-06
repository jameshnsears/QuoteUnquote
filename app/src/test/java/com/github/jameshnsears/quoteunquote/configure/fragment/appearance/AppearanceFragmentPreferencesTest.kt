package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.AppearanceContentsFragmentDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text.AppearanceTextDialogAuthorDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text.AppearanceTextDialogPositionDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.contents.text.AppearanceTextDialogQuotationDouble
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
        with(launchFragment<AppearanceContentsFragmentDouble>()) {
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

    @Test
    fun confirmInitialTextPreferencesQuotation() {
        with(launchFragment<AppearanceTextDialogQuotationDouble>()) {
            onFragment { fragment ->
                fragment.setTextColour()
                assertEquals(
                    "#FF000000",
                    fragment.appearancePreferences?.appearanceQuotationTextColour,
                )

                fragment.setTextSize()
                assertThat(fragment.appearancePreferences?.appearanceQuotationTextSize, equalTo(16))
            }
        }
    }

    @Test
    fun confirmInitialTextPreferencesAuthor() {
        with(launchFragment<AppearanceTextDialogAuthorDouble>()) {
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
    fun confirmInitialTextPreferencesPosition() {
        with(launchFragment<AppearanceTextDialogPositionDouble>()) {
            onFragment { fragment ->
                fragment.setTextColour()
                assertEquals(
                    "#FF000000",
                    fragment.appearancePreferences?.appearancePositionTextColour,
                )

                fragment.setTextSize()
                assertThat(fragment.appearancePreferences?.appearancePositionTextSize, equalTo(16))

                fragment.setTextHide()
                assertTrue(fragment.appearancePreferences?.appearancePositionTextHide == false)
            }
        }
    }

    @Test
    fun confirmInitialToolbarPreferences() {
        with(launchFragment<AppearanceToolbarFragmentDouble>()) {
            onFragment { fragment ->
                fragment.setHideSeparator()
                assertTrue(fragment.appearancePreferences?.appearanceToolbarHideSeparator == true)

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
                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchHideSeparator?.isChecked =
                    false
                assertTrue(fragment.appearancePreferences?.appearanceToolbarHideSeparator == false)

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
        with(launchFragment<AppearanceContentsFragmentDouble>()) {
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
        with(launchFragment<AppearanceContentsFragmentDouble>()) {
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
