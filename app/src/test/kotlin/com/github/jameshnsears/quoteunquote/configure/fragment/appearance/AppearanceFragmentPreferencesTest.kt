package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import android.app.Application
import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.AppearanceStyleFragmentDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style.dialog.StyleDialogAuthorDouble
import com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.toolbar.AppearanceToolbarFragmentDouble
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import com.github.jameshnsears.quoteunquote.utils.widget.WidgetIdHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class AppearanceFragmentPreferencesTest : ShadowLoggingHelper() {
    @Test
    fun confirmInitialAppearanceStylePreferences() {
        launchFragment<AppearanceStyleFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.setBackgroundColour()
                assertThat(fragment.appearancePreferences?.appearanceColour, equalTo("#FFFFFFFF"))

                fragment.setTransparency()
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(-1))

                fragment.setTextFamily()
                assertThat(
                    fragment.appearancePreferences?.appearanceTextFamily,
                    equalTo("Sans Serif"),
                )

                fragment.setTextStyle()
                assertThat(
                    fragment.appearancePreferences?.appearanceTextStyle,
                    equalTo("Italic"),
                )
            }
        }
    }

    @Test
    fun confirmInitialTextPreferencesAuthor() {
        launchFragment<StyleDialogAuthorDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.setTextColour()
                assertThat(
                    fragment.appearancePreferences?.appearanceAuthorTextColour,
                    equalTo("#FF000000"),
                )

                fragment.setTextSize()
                assertThat(fragment.appearancePreferences?.appearanceAuthorTextSize, equalTo(18))

                fragment.setTextHide()
                assertThat(fragment.appearancePreferences?.appearanceAuthorTextHide, `is`(false))
            }
        }
    }

    @Test
    fun confirmInitialToolbarPreferences() {
        launchFragment<AppearanceToolbarFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.setToolbarColour()
                assertThat(
                    fragment.appearancePreferences?.appearanceToolbarColour,
                    equalTo("#FF000000"),
                )

                fragment.setToolbar()
                assertThat(fragment.appearancePreferences?.appearanceToolbarFirst, `is`(false))
                assertThat(fragment.appearancePreferences?.appearanceToolbarPrevious, `is`(true))
                assertThat(fragment.appearancePreferences?.appearanceToolbarFavourite, `is`(true))
                assertThat(fragment.appearancePreferences?.appearanceToolbarShare, `is`(false))
                assertThat(fragment.appearancePreferences?.appearanceToolbarJump, `is`(true))
                assertThat(fragment.appearancePreferences?.appearanceToolbarRandom, `is`(true))
                assertThat(fragment.appearancePreferences?.appearanceToolbarSequential, `is`(false))
            }
        }
    }

    @Test
    fun confirmToolbarChangesToPreferences() {
        launchFragment<AppearanceToolbarFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchFirst?.isChecked = true
                assertThat(fragment.appearancePreferences?.appearanceToolbarFirst, `is`(true))

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchPrevious?.isChecked =
                    false
                assertThat(fragment.appearancePreferences?.appearanceToolbarPrevious, `is`(false))

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchToggleFavourite?.isChecked =
                    false
                assertThat(fragment.appearancePreferences?.appearanceToolbarFavourite, `is`(false))

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchShare?.isChecked = false
                assertThat(fragment.appearancePreferences?.appearanceToolbarShare, `is`(false))

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchNextRandom?.isChecked =
                    false
                assertThat(fragment.appearancePreferences?.appearanceToolbarRandom, `is`(false))

                fragment.fragmentAppearanceTabToolbarBinding?.toolbarSwitchNextSequential?.isChecked =
                    true
                assertThat(fragment.appearancePreferences?.appearanceToolbarSequential, `is`(true))
            }
        }
    }

    @Test
    fun emptyDeletedStylePreferences() {
        launchFragment<AppearanceStyleFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.appearancePreferences?.appearanceTransparency = 5
                fragment.setTransparency()
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(5))

                AppearancePreferences.delete(getApplicationContext(), WidgetIdHelper.WIDGET_ID_02)
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(5))

                AppearancePreferences.delete(getApplicationContext(), WidgetIdHelper.WIDGET_ID_01)
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(-1))
            }
        }
    }

    @Test
    fun emptyDisabledPreferences() {
        launchFragment<AppearanceStyleFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.appearancePreferences?.appearanceTransparency = 5
                fragment.setTransparency()
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(5))

                AppearancePreferences.erase(getApplicationContext())
                assertThat(fragment.appearancePreferences?.appearanceTransparency, equalTo(-1))
            }
        }
    }
}
