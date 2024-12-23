package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.content.Context;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.jameshnsears.quoteunquote.configure.fragment.quotations.QuotationsPreferences;

import timber.log.Timber;

public class FragmentCommon extends Fragment {
    public int widgetId;

    public FragmentCommon() {
        // ...
    }

    public FragmentCommon(final int theWidgetId) {
        widgetId = theWidgetId;
        Timber.d("widgetId=%d", widgetId);
    }

    public void makeButtonAlpha(@NonNull final Button button, final boolean enable) {
        button.setAlpha(enable ? 1 : 0.25f);
    }

    public void rememberScreen(final Screen screen, Context applicationContext) {
        new QuotationsPreferences(widgetId, applicationContext).setScreen(screen.screenName);
    }

    public enum Screen {
        QuotationsFilter("QuotationsFilter"),
        ContentInternal("ContentInternal"),
        ContentFiles("ContentFiles"),
        ContentWeb("ContentWeb"),

        AppearanceStyle("AppearanceStyle"),
        AppearanceToolbar("AppearanceToolbar"),

        Notifications("Notifications"),

        Sync("Sync");

        public final String screenName;

        Screen(String screenName) {
            this.screenName = screenName;
        }

        public static Screen fromString(String name) {
            for (Screen screen : Screen.values()) {
                if (screen.screenName.equalsIgnoreCase(name)) {
                    return screen;
                }
            }
            throw new IllegalArgumentException("No enum constant with name " + name);
        }
    }
}
