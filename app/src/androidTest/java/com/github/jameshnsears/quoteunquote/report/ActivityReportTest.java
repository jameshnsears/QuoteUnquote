package com.github.jameshnsears.quoteunquote.report;

import android.widget.Spinner;

import com.github.jameshnsears.quoteunquote.DatabaseTestHelper;
import com.github.jameshnsears.quoteunquote.R;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.ConcurrentHashMap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ActivityReportTest extends DatabaseTestHelper {
    @Rule
    public ActivityTestRule<ActivityReport> activityRule =
            new ActivityTestRule(ActivityReport.class);

    @Test
    public void reportQuotation() {
        insertTestDataSet01();

        setDefaultQuotation();

        quoteUnquoteModel.markAsReported(widgetID);

        assertTrue("", quoteUnquoteModel.isReported(widgetID));
    }

    @Test
    public void itemsInSpinner() {
        final Spinner spinnerReason = activityRule.getActivity().findViewById(R.id.spinnerReason);
        assertThat("", spinnerReason.getAdapter().getCount(), Is.is(6));
    }

    @Test
    public void hasQuotationAlreadyBeenReported() {
        assertFalse("", activityRule.getActivity().hasQuotationAlreadyBeenReported());
    }

    @Test
    public void auditProperties() {
        final ConcurrentHashMap<String, String> auditProperties = activityRule.getActivity().getAuditProperties();
        assertEquals(
                "",
                "digest=1624c314; author=Arthur Balfour; reason=Attribution; notes=", auditProperties.get("Report"));
    }
}
