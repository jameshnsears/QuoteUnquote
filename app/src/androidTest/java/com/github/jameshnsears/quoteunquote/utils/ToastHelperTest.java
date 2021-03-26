package com.github.jameshnsears.quoteunquote.utils;

import android.widget.Toast;

import com.github.jameshnsears.quoteunquote.report.ActivityReport;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class ToastHelperTest {
    @Rule
    public ActivityTestRule<ActivityReport> activityRule =
            new ActivityTestRule(ActivityReport.class);

    @Test
    @UiThreadTest
    public void makeToast() {
        assertNull("", ToastHelper.toast);

        ToastHelper.makeToast(
                activityRule.getActivity().getApplicationContext(),
                "msg",
                Toast.LENGTH_SHORT);

        assertNotNull("", ToastHelper.toast);
    }
}
