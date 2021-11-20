package com.github.jameshnsears.quoteunquote

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.github.jameshnsears.quoteunquote.databinding.ActivityInstructionsBinding
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper

class QuoteUnquoteInstructions : AppCompatActivity() {
    private lateinit var activityInstructionsBinding: ActivityInstructionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityInstructionsBinding = ActivityInstructionsBinding.inflate(layoutInflater)
        val view = activityInstructionsBinding.root
        setContentView(view)

        // better support for Amazon Appstore
        val buildconfigVersion = BuildConfig.VERSION_NAME.replace("-googleplay", "")

        this.activityInstructionsBinding.textViewVersion.text = this.resources.getString(
            R.string.activity_instructions_version,
            buildconfigVersion, BuildConfig.GIT_HASH
        )

        val layoutFooter: LinearLayout = this.activityInstructionsBinding.layoutFooter
        layoutFooter.setOnClickListener {
            this.startActivity(
                IntentFactoryHelper.createIntentActionView()
            )
        }
    }
}
