package com.github.jameshnsears.quoteunquote.db

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.BAKLAVA])
class DatabaseQuotationsRandomTest : ShadowLoggingHelper() {
    @Test
    fun getRandomIndex() {
        val databaseRepository = DatabaseRepository.getInstance(getApplicationContext())
        val stringList: MutableList<String> = ArrayList()

        stringList.add("1")
        stringList.add("2")
        stringList.add("3")
        stringList.add("4")
        stringList.add("5")

        repeat(stringList.size) {
            val rndIndex = databaseRepository.getRandomIndex(stringList)
            Timber.d("" + rndIndex)

            assertThat(rndIndex >= 0, `is`(true))
            assertThat(rndIndex <= stringList.size, `is`(true))
        }
    }
}
