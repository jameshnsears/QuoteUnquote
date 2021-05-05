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

    public FragmentCommon(int theWidgetId) {
        this.widgetId = theWidgetId;
    }

    protected void setSpinner(
            @NonNull Spinner spinner,
            @NonNull BaseAdapter spinnerAdapter,
            @NonNull String preference,
            final int defaultSelection,
            final int resourceArrayId) {

        spinner.setAdapter(spinnerAdapter);

        if ("".equals(preference)) {
            spinner.setSelection(defaultSelection);
        } else {
            int selectionIndex = 0;
            for (String spinnerRow : this.getActivity().getBaseContext().getResources().getStringArray(resourceArrayId)) {
                if (spinnerRow.equals(preference)) {
                    spinner.setSelection(selectionIndex);
                    break;
                }
                selectionIndex++;
            }
        }
    }
}
