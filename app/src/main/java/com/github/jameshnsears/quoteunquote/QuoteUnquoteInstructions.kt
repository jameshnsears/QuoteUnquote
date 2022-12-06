package com.github.jameshnsears.quoteunquote

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.github.jameshnsears.quoteunquote.databinding.ActivityInstructionsBinding
import com.github.jameshnsears.quoteunquote.utils.IntentFactoryHelper

class QuoteUnquoteInstructions : AppCompatActivity() {
    private lateinit var activityInstructionsBinding: ActivityInstructionsBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.notification_permission_not_allowed),
                Toast.LENGTH_LONG,
            ).show()
        } else {
            Toast.makeText(
                applicationContext,
                applicationContext.getString(R.string.notification_permission_allowed),
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityInstructionsBinding = ActivityInstructionsBinding.inflate(layoutInflater)
        val view = activityInstructionsBinding.root
        setContentView(view)

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

        val layoutNotificationPerission: LinearLayout =
            this.activityInstructionsBinding.layoutNotificationPermission

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                layoutNotificationPerission.setOnClickListener {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            layoutNotificationPerission.visibility = View.GONE
        }
    }
}
