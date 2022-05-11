package com.github.jameshnsears.quoteunquote.configure.fragment;

import android.widget.BaseAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FragmentCommon extends Fragment {
    public int widgetId;

    public FragmentCommon() {
        // ...
    }

    public FragmentCommon(final int theWidgetId) {
        widgetId = theWidgetId;
    }

    protected void setSpinner(
            @NonNull final Spinner spinner,
            @NonNull final BaseAdapter spinnerAdapter,
            @NonNull final String preference,
            int defaultSelection,
            int resourceArrayId) {

        spinner.setAdapter(spinnerAdapter);

        if ("".equals(preference)) {
            spinner.setSelection(defaultSelection);
        } else {
            int selectionIndex = 0;
            for (final String spinnerRow : getActivity().getBaseContext().getResources().getStringArray(resourceArrayId)) {
                if (spinnerRow.equals(preference)) {
                    spinner.setSelection(selectionIndex);
                    break;
                }
                selectionIndex++;
            }
        }
    }
}
