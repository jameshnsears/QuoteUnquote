package com.github.jameshnsears.quoteunquote.utils.audit;

import android.app.Application;

import java.util.concurrent.ConcurrentHashMap;

import timber.log.Timber;

public final class AuditEventHelper {
    public static synchronized void createInstance(final Application application) {
        Timber.d("not implemented");
    }

    public static void auditEvent(final String auditEvent, final ConcurrentHashMap<String, String> properties) {
        Timber.d("not implemented");
    }
}
