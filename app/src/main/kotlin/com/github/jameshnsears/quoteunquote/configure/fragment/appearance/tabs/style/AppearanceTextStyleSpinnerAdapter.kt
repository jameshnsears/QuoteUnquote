package com.github.jameshnsears.quoteunquote.configure.fragment.appearance.tabs.style

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.github.jameshnsears.quoteunquote.R

class AppearanceTextStyleSpinnerAdapter(private val context: Context) : BaseAdapter() {
    var style: Array<String> =
        context.resources.getStringArray(R.array.fragment_appearance_style_array)

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
                android.R.layout.simple_spinner_dropdown_item,
                viewGroup,
                false,
            )
        }

        view = view?.findViewById<TextView>(android.R.id.text1)!!
        view.text = getItem(position)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    view.setTextColor(Color.WHITE)
                }
            }
        } else {
            view.setTextColor(Color.BLACK)
        }

        when (getItem(position)) {
            "Bold" -> view.setTypeface(null, Typeface.BOLD)
            "Bold Italic" -> view.setTypeface(null, Typeface.BOLD_ITALIC)
            "Italic" -> view.setTypeface(null, Typeface.ITALIC)
            "Italic, Shadow" -> {
                view.setTypeface(null, Typeface.ITALIC)
                view.setShadowLayer(1F, 2F, 2F, Color.BLACK)
            }

            "Regular" -> view.setTypeface(null, Typeface.NORMAL)
            "Regular, Shadow" -> {
                view.setTypeface(null, Typeface.NORMAL)
                view.setShadowLayer(1F, 2F, 2F, Color.BLACK)
            }
        }

        return view
    }
}
