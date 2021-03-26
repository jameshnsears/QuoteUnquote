package com.github.jameshnsears.quoteunquote.audit;

import android.app.Application;

import com.github.jameshnsears.quoteunquote.BuildConfig;
import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

import java.util.Map;

public final class AuditEventHelper {
    public static final String REPORT = "Report";
    public static final String AUTHOR = "Author";
    public static final String FAVOURITE = "Favourite";
    public static final String FAVOURITE_SEND = "FavouriteSend";
    public static final String FAVOURITE_RECEIVE = "FavouriteReceive";
    public static final String QUOTATION = "Quotation";

    private static AuditEventHelper auditEventHelperSingleton;

    private AuditEventHelper(final Application application) {
        AppCenter.start(application, BuildConfig.APPCENTER_KEY,
                Analytics.class, Crashes.class);
    }

    public static synchronized void createInstance(final Application application) {
        if (auditEventHelperSingleton == null) {
            auditEventHelperSingleton = new AuditEventHelper(application);
        }
    }

    public static void auditAppCenter(final String event, final Map<String, String> properties, final int flag) {
        Analytics.trackEvent(event, properties, flag);
    }
}
