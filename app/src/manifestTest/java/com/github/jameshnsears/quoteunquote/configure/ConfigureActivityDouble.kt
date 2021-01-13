package com.github.jameshnsears.quoteunquote.configure

import androidx.annotation.NonNull
import com.github.jameshnsears.quoteunquote.configure.fragment.content.ContentFragmentDouble

class ConfigureActivityDouble : ConfigureActivity() {
    override fun broadcastFinishIntent() {
        // don't broadcast anything for test(s)
    }

    @NonNull
    override fun getFragmentContentNewInstance(): ContentFragmentDouble {
        return ContentFragmentDouble.newInstance(widgetId)
    }
}
