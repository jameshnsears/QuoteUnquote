package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import com.github.jameshnsears.quoteunquote.R

class AppearanceTextStyleSpinnerAdapter(@NonNull private val context: Context) : BaseAdapter() {
    var style: Array<String> = context.resources.getStringArray(R.array.fragment_appearance_style_array)

    override fun getCount(): Int {
        return style.size
    }

    override fun getItem(position: Int): String {
        var positionIndex = 0

        var foundItem = ""

        for (item in style) {
            if (positionIndex == position) {
                foundItem = item
                break
            }
            positionIndex++
        }

        return foundItem
    }

    override fun getItemId(id: Int): Long {
        return id.toLong()
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup?): View {
        var view: View? = convertView

        if (view == null) {
            view = LayoutInflater.from(context).inflate(
                android.R.layout.simple_spinner_dropdown_item, viewGroup, false
            )
        }

        view = view?.findViewById(android.R.id.text1) as TextView
        view.text = getItem(position)

        when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                view.setTextColor(Color.WHITE)
            }
        }

        when (getItem(position)) {
            "Bold" -> view.setTypeface(null, Typeface.BOLD)
            "Bold Italic" -> view.setTypeface(null, Typeface.BOLD_ITALIC)
            "Italic" -> view.setTypeface(null, Typeface.ITALIC)
            "Regular" -> view.setTypeface(null, Typeface.NORMAL)
        }

        return view
    }
}
