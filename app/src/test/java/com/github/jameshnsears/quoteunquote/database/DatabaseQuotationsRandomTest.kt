package com.github.jameshnsears.quoteunquote.database

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.jameshnsears.quoteunquote.utils.logging.MethodLineLoggingTree
import com.github.jameshnsears.quoteunquote.utils.logging.ShadowLoggingHelper
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class DatabaseQuotationsRandomTest : ShadowLoggingHelper() {
    @Test
    fun getRandomIndex() {
        if (Timber.treeCount() == 0) {
            Timber.plant(MethodLineLoggingTree())
        }

        val databaseRepository = DatabaseRepository.getInstance(getApplicationContext())
        val stringList: MutableList<String> = ArrayList()

        stringList.add("1")
        stringList.add("2")
        stringList.add("3")
        stringList.add("4")
        stringList.add("5")

        for (i in stringList.indices) {
            val rndIndex = databaseRepository.getRandomIndex(stringList)
            Timber.d("" + rndIndex)
            if (rndIndex < 0 || rndIndex > stringList.size) {
                fail(rndIndex.toString())
            }
        }

        databaseRepository.close()
    }
}
