package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style;

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

class AppearanceBackgroundColourSpinnerAdapter extends BaseAdapter {
    @NonNull
    private final List<String> colours;
    @NonNull
    private final Context context;

    AppearanceBackgroundColourSpinnerAdapter(@NonNull Context activityContext) {
        this.context = activityContext;
        this.colours = new ArrayList<>();
        this.colours.addAll(Arrays.asList(activityContext.getResources().getStringArray(R.array.fragment_appearance_colour_array)));
    }

    @Override
    public int getCount() {
        return this.colours.size();
    }

    @Override
    public Object getItem(int arg0) {
        return this.colours.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, final View convertView, ViewGroup viewGroup) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(this.context).inflate(android.R.layout.simple_spinner_dropdown_item, null);
        }

        TextView textView = view.findViewById(android.R.id.text1);
        textView.setBackgroundColor(Color.parseColor(this.colours.get(position)));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setText(" ");

        return view;
    }
}
