package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter

import android.app.Application
import android.os.Build
import androidx.fragment.app.testing.launchFragment
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.R
import com.github.jameshnsears.quoteunquote.utils.ContentSelection
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA], application = Application::class)
class QuotationsFilterFragmentTest : ShadowLoggingHelper() {
    @Before
    fun setUpRx() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setComputationSchedulerHandler { Schedulers.trampoline() }
        RxJavaPlugins.setNewThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @After
    fun tearDownRx() {
        RxJavaPlugins.reset()
    }

    @Test
    fun confirmInitialFilterPreferences() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                val preferences = fragment.quotationsPreferences!!

                assertThat(
                    fragment.fragmentQuotationsTabFilterBinding!!.radioButtonAll.isChecked,
                    equalTo(preferences.contentSelection == ContentSelection.ALL),
                )
                assertThat(
                    fragment.fragmentQuotationsTabFilterBinding!!.radioButtonAuthorIndividual.isChecked,
                    equalTo(preferences.contentSelection == ContentSelection.AUTHOR),
                )
                assertThat(
                    fragment.fragmentQuotationsTabFilterBinding!!.radioButtonFavourites.isChecked,
                    equalTo(preferences.contentSelection == ContentSelection.FAVOURITES),
                )
                assertThat(
                    fragment.fragmentQuotationsTabFilterBinding!!.radioButtonSearch.isChecked,
                    equalTo(preferences.contentSelection == ContentSelection.SEARCH),
                )
            }
        }
    }

    @Test
    fun selectAuthorFilter() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonAuthorIndividual.isChecked = true

                assertThat(fragment.quotationsPreferences!!.contentSelection, `is`(ContentSelection.AUTHOR))
                assertThat(fragment.fragmentQuotationsTabFilterBinding!!.spinnerAuthors.isEnabled, `is`(true))
                assertThat(fragment.fragmentQuotationsTabFilterBinding!!.spinnerAuthorsCount.isEnabled, `is`(true))
            }
        }
    }

    @Test
    fun selectSearchFilter() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonSearch.isChecked = true

                assertThat(fragment.quotationsPreferences!!.contentSelection, `is`(ContentSelection.SEARCH))
                assertThat(fragment.fragmentQuotationsTabFilterBinding!!.editTextSearchText.isEnabled, `is`(true))
                assertThat(fragment.fragmentQuotationsTabFilterBinding!!.switchRegEx.isEnabled, `is`(true))
            }
        }
    }

    @Test
    fun selectFavouritesFilter() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonFavourites.isEnabled = true
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonFavourites.isChecked = true

                assertThat(fragment.quotationsPreferences!!.contentSelection, `is`(ContentSelection.FAVOURITES))
            }
        }
    }

    @Test
    fun searchTextChanges() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonSearch.isChecked = true

                val editText = fragment.fragmentQuotationsTabFilterBinding!!.editTextSearchText
                editText.setText("test search")

                // debounce is 25ms in QuotationsFilterFragment
                ShadowLooper.idleMainLooper(50, TimeUnit.MILLISECONDS)

                assertThat(fragment.quotationsPreferences!!.contentSelectionSearch, equalTo("test search"))
            }
        }
    }

    @Test
    fun exclusionTextChanges() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonAll.isChecked = true

                val editText = fragment.fragmentQuotationsTabFilterBinding!!.editTextResultsExclusion
                editText.setText("excluded words")

                // debounce is 25ms
                ShadowLooper.idleMainLooper(50, TimeUnit.MILLISECONDS)

                assertThat(fragment.quotationsPreferences!!.contentSelectionAllExclusion, equalTo("excluded words"))
            }
        }
    }

    @Test
    fun exportFavouritesClick() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.buttonFavouritesExport.isEnabled = true
                fragment.fragmentQuotationsTabFilterBinding!!.buttonFavouritesExport.performClick()

                val shadowActivity = shadowOf(fragment.requireActivity())
                val intent = shadowActivity.nextStartedActivity
                assertThat(intent.action, equalTo(android.content.Intent.ACTION_CREATE_DOCUMENT))
                assertThat(intent.type, equalTo("text/csv"))
                assertThat(intent.getStringExtra(android.content.Intent.EXTRA_TITLE), equalTo("Favourite.csv"))
            }
        }
    }

    @Test
    fun changeAuthorCountSpinner() {
        launchFragment<QuotationsFilterFragmentDouble>(themeResId = R.style.AppTheme).use { scenario ->
            scenario.onFragment { fragment ->
                fragment.fragmentQuotationsTabFilterBinding!!.radioButtonAuthorIndividual.isChecked = true

                // The spinner is populated by setDisposableCardSourceCount() which is called in initUI().
                // We need to idle the looper to let Rx updates finish.
                ShadowLooper.idleMainLooper()

                val spinner = fragment.fragmentQuotationsTabFilterBinding!!.spinnerAuthorsCount
                if (spinner.count > 0) {
                    spinner.setSelection(0)
                    val selectedValue = spinner.selectedItem.toString()
                    assertThat(fragment.quotationsPreferences!!.contentSelectionAuthorCount, equalTo(selectedValue.toInt()))
                }
            }
        }
    }
}
