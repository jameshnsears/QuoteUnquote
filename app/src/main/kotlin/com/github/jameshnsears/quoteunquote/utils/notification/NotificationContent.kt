package com.github.jameshnsears.quoteunquote.utils.notification

import android.content.Context

data class NotificationContent(
    val context: Context,
    val widgetId: Int,
    val author: String,
    val quotation: String,
    val digest: String,
    val isFavourite: Boolean,
    val sequential: Boolean,
    var notificationId: Int,
    val notificationEvent: String,
)
