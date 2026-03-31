package com.github.jameshnsears.quoteunquote

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.jameshnsears.quoteunquote.databinding.ActivityInstructionsBinding
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper

class QuoteUnquoteInstructions : AppCompatActivity() {
    private lateinit var activityInstructionsBinding: ActivityInstructionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityInstructionsBinding = ActivityInstructionsBinding.inflate(layoutInflater)
        val view = activityInstructionsBinding.root
        setContentView(view)

        val controller = WindowCompat.getInsetsController(window, window.decorView)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        val buildconfigVersion = BuildConfig.VERSION_NAME

        this.activityInstructionsBinding.textViewVersion.text = this.resources.getString(
            R.string.activity_instructions_version,
            buildconfigVersion,
            BuildConfig.GIT_HASH,
        )

        val layoutFooter: LinearLayout = this.activityInstructionsBinding.layoutFooter
        layoutFooter.setOnClickListener {
            this.startActivity(
                IntentFactoryHelper.createIntentActionView("http://github.com/jameshnsears/quoteunquote"),
            )
        }
    }
}
