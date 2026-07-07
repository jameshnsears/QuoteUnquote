package com.github.jameshnsears.quoteunquote

import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.jameshnsears.quoteunquote.databinding.ActivityInstructionsBinding
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper

class QuoteUnquoteInstructions : AppCompatActivity() {
    private lateinit var activityInstructionsBinding: ActivityInstructionsBinding
    private var scrollPositionY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        activityInstructionsBinding = ActivityInstructionsBinding.inflate(layoutInflater)
        setContentView(activityInstructionsBinding.root)

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        windowInsetsController.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val appBarLayout = findViewById<android.view.View>(R.id.appBarLayout)
            appBarLayout.setPadding(
                appBarLayout.paddingLeft,
                systemBars.top,
                appBarLayout.paddingRight,
                appBarLayout.paddingBottom,
            )

            val footer = findViewById<android.view.View>(R.id.layoutFooterContainer)
            footer.setPadding(
                footer.paddingLeft,
                footer.paddingTop,
                footer.paddingRight,
                systemBars.bottom,
            )
            insets
        }

        val buildconfigVersion = BuildConfig.VERSION_NAME

        this.activityInstructionsBinding.textViewVersion.text =
            this.resources.getString(
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

        // Restore scroll position after layout is measured
        if (savedInstanceState != null) {
            scrollPositionY = savedInstanceState.getInt("scrollPositionY", 0)
            activityInstructionsBinding.scrollViewInstructions.post {
                activityInstructionsBinding.scrollViewInstructions.scrollTo(0, scrollPositionY)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(
            "scrollPositionY",
            activityInstructionsBinding.scrollViewInstructions.scrollY,
        )
    }
}
