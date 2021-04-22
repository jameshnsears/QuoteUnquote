package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.github.jameshnsears.quoteunquote.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class AppearanceTextColourSpinnerAdapter extends BaseAdapter {
    @NonNull
    private final List<String> colours;
    @NonNull
    private final Context context;

    AppearanceTextColourSpinnerAdapter(@NonNull final Context activityContext) {
        context = activityContext;
        colours = new ArrayList<>();
        colours.addAll(Arrays.asList(activityContext.getResources().getStringArray(R.array.fragment_appearance_text_colour_array)));
    }

    @Override
    public int getCount() {
        return colours.size();
    }

    @Override
    public Object getItem(final int arg0) {
        return colours.get(arg0);
    }

    @Override
    public long getItemId(final int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup viewGroup) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_spinner_dropdown_item, null);
        }

        final TextView textView = view.findViewById(android.R.id.text1);
        textView.setBackgroundColor(Color.parseColor(colours.get(position)));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setText(" ");

        return view;
    }
}
