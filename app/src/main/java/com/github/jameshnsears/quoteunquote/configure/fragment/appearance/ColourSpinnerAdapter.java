package com.github.jameshnsears.quoteunquote.configure.fragment.appearance;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.github.jameshnsears.quoteunquote.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ColourSpinnerAdapter extends BaseAdapter {
    private final List<String> colours;
    private final Context context;

    public ColourSpinnerAdapter(final Context context) {
        this.context = context;
        colours = new ArrayList<>();
        colours.addAll(Arrays.asList(context.getResources().getStringArray(R.array.fragment_appearance_colour_array)));
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
    public View getView(final int position, View view, final ViewGroup viewGroup) {
        final LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        final TextView textView = view.findViewById(android.R.id.text1);
        textView.setBackgroundColor(Color.parseColor(colours.get(position)));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        textView.setText(" ");
        return view;
    }
}