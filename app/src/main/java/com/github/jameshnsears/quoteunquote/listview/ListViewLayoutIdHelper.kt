package com.github.jameshnsears.quoteunquote.listview

import com.github.jameshnsears.quoteunquote.R

class ListViewLayoutIdHelper {
    private constructor() {
        // ...
    }

    companion object {
        fun layoutIdForCursive(
            textSize: String
        ): Int {
            return when (textSize) {
                "Bold" -> R.layout.listvew_row_0_cursive_bold
                "Bold Italic" -> R.layout.listvew_row_0_cursive_bold_italic
                "Italic" -> R.layout.listvew_row_0_cursive_italic
                else -> R.layout.listvew_row_0_cursive
            }
        }

        fun layoutIdForMonospace(
            textSize: String
        ): Int {
            return when (textSize) {
                "Bold" -> R.layout.listvew_row_1_monospace_bold
                "Bold Italic" -> R.layout.listvew_row_1_monospace_bold_italic
                "Italic" -> R.layout.listvew_row_1_monospace_italic
                else -> R.layout.listvew_row_1_monospace
            }
        }

        fun layoutIdForSansSerif(
            textSize: String
        ): Int {
            return when (textSize) {
                "Bold" -> R.layout.listvew_row_2_sans_serif_bold
                "Bold Italic" -> R.layout.listvew_row_2_sans_serif_bold_italic
                "Italic" -> R.layout.listvew_row_2_sans_serif_italic
                else -> R.layout.listvew_row_2_sans_serif
            }
        }

        fun layoutIdForSansSerifCondensed(
            textSize: String
        ): Int {
            return when (textSize) {
                "Bold" -> R.layout.listvew_row_3_sans_serif_condensed_bold
                "Bold Italic" -> R.layout.listvew_row_3_sans_serif_condensed_bold_italic
                "Italic" -> R.layout.listvew_row_3_sans_serif_condensed_italic
                else -> R.layout.listvew_row_3_sans_serif_condensed
            }
        }

        fun layoutIdForSansSerifMedium(
            textSize: String
        ): Int {
            return when (textSize) {
                "Bold" -> R.layout.listvew_row_4_sans_serif_medium_bold
                "Bold Italic" -> R.layout.listvew_row_4_sans_serif_medium_bold_italic
                "Italic" -> R.layout.listvew_row_4_sans_serif_medium_italic
                else -> R.layout.listvew_row_4_sans_serif_medium
            }
        }

        fun layoutIdForSerif(
            textSize: String
        ): Int {
            return when (textSize) {
                "Bold" -> R.layout.listvew_row_5_serif_bold
                "Bold Italic" -> R.layout.listvew_row_5_serif_bold_italic
                "Italic" -> R.layout.listvew_row_5_serif_italic
                else -> R.layout.listvew_row_5_serif
            }
        }
    }
}
