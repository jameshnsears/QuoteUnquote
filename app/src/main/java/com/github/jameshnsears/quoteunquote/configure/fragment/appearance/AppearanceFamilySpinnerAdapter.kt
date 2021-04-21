package com.github.jameshnsears.quoteunquote.configure.fragment.appearance

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.annotation.NonNull
import com.github.jameshnsears.quoteunquote.R

class AppearanceFamilySpinnerAdapter(@NonNull private val context: Context) : BaseAdapter() {
    var family = context.resources.getStringArray(R.array.fragment_appearance_family_array)

    override fun getCount(): Int {
        return family.size
    }

    override fun getItem(position: Int): String {
        var positionIndex = 0

        var foundItem = ""

        for (item in family) {
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

        when (getItem(position)) {
            "Cursive"
            -> view.typeface = Typeface.createFromFile("/system/fonts/DancingScript-Regular.ttf")
            "Monospace"
            -> view.typeface = Typeface.createFromFile("/system/fonts/DroidSansMono.ttf")
            "Sans Serif"
            -> view.typeface = Typeface.createFromFile("/system/fonts/Roboto-Regular.ttf")
            "Sans Serif Condensed"
            -> view.typeface = Typeface.createFromFile("/system/fonts/RobotoCondensed-Regular.ttf")
            "Sans Serif Medium"
            -> view.typeface = Typeface.createFromFile("/system/fonts/Roboto-Medium.ttf")
            "Serif"
            -> view.typeface = Typeface.createFromFile("/system/fonts/NotoSerif-Regular.ttf")
        }

        return view
    }
}
