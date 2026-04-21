package com.github.jameshnsears.quoteunquote.listview

import com.github.jameshnsears.quoteunquote.R

class ListViewLayoutIdHelper {
    companion object {
        fun layoutIdForCursive(
            textStyle: String,
            center: Boolean,
            rightSource: Boolean,
        ): Int {
            if (rightSource) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_0_cursive_bold_right_source
                    "Bold Italic" -> R.layout.listvew_row_0_cursive_bold_italic_right_source
                    "Italic" -> R.layout.listvew_row_0_cursive_italic_right_source
                    "Italic, Shadow" -> R.layout.listvew_row_0_cursive_italic_shadow_right_source
                    "Regular, Shadow" -> R.layout.listvew_row_0_cursive_shadow_right_source
                    else -> R.layout.listvew_row_0_cursive_right_source
                }
            } else if (center) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_0_cursive_bold_center
                    "Bold Italic" -> R.layout.listvew_row_0_cursive_bold_italic_center
                    "Italic" -> R.layout.listvew_row_0_cursive_italic_center
                    "Italic, Shadow" -> R.layout.listvew_row_0_cursive_italic_shadow_center
                    "Regular, Shadow" -> R.layout.listvew_row_0_cursive_shadow_center
                    else -> R.layout.listvew_row_0_cursive_center
                }
            } else {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_0_cursive_bold
                    "Bold Italic" -> R.layout.listvew_row_0_cursive_bold_italic
                    "Italic" -> R.layout.listvew_row_0_cursive_italic
                    "Italic, Shadow" -> R.layout.listvew_row_0_cursive_italic_shadow
                    "Regular, Shadow" -> R.layout.listvew_row_0_cursive_shadow
                    else -> R.layout.listvew_row_0_cursive
                }
            }
        }

        fun layoutIdForMonospace(
            textStyle: String,
            center: Boolean,
            rightSource: Boolean,
        ): Int {
            if (rightSource) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_1_monospace_bold_right_source
                    "Bold Italic" -> R.layout.listvew_row_1_monospace_bold_italic_right_source
                    "Italic" -> R.layout.listvew_row_1_monospace_italic_right_source
                    "Italic, Shadow" -> R.layout.listvew_row_1_monospace_italic_shadow_right_source
                    "Regular, Shadow" -> R.layout.listvew_row_1_monospace_shadow_right_source
                    else -> R.layout.listvew_row_1_monospace_right_source
                }
            } else if (center) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_1_monospace_bold_center
                    "Bold Italic" -> R.layout.listvew_row_1_monospace_bold_italic_center
                    "Italic" -> R.layout.listvew_row_1_monospace_italic_center
                    "Italic, Shadow" -> R.layout.listvew_row_1_monospace_italic_shadow_center
                    "Regular, Shadow" -> R.layout.listvew_row_1_monospace_shadow_center
                    else -> R.layout.listvew_row_1_monospace_center
                }
            } else {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_1_monospace_bold
                    "Bold Italic" -> R.layout.listvew_row_1_monospace_bold_italic
                    "Italic" -> R.layout.listvew_row_1_monospace_italic
                    "Italic, Shadow" -> R.layout.listvew_row_1_monospace_italic_shadow
                    "Regular, Shadow" -> R.layout.listvew_row_1_monospace_shadow
                    else -> R.layout.listvew_row_1_monospace
                }
            }
        }

        fun layoutIdForSansSerif(
            textStyle: String,
            center: Boolean,
            rightSource: Boolean,
        ): Int {
            if (rightSource) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_2_sans_serif_bold_right_source
                    "Bold Italic" -> R.layout.listvew_row_2_sans_serif_bold_italic_right_source
                    "Italic" -> R.layout.listvew_row_2_sans_serif_italic_right_source
                    "Italic, Shadow" -> R.layout.listvew_row_2_sans_serif_italic_shadow_right_source
                    "Regular, Shadow" -> R.layout.listvew_row_2_sans_serif_shadow_right_source
                    else -> R.layout.listvew_row_2_sans_serif_right_source
                }
            } else if (center) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_2_sans_serif_bold_center
                    "Bold Italic" -> R.layout.listvew_row_2_sans_serif_bold_italic_center
                    "Italic" -> R.layout.listvew_row_2_sans_serif_italic_center
                    "Italic, Shadow" -> R.layout.listvew_row_2_sans_serif_italic_shadow_center
                    "Regular, Shadow" -> R.layout.listvew_row_2_sans_serif_shadow_center
                    else -> R.layout.listvew_row_2_sans_serif_center
                }
            } else {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_2_sans_serif_bold
                    "Bold Italic" -> R.layout.listvew_row_2_sans_serif_bold_italic
                    "Italic" -> R.layout.listvew_row_2_sans_serif_italic
                    "Italic, Shadow" -> R.layout.listvew_row_2_sans_serif_italic_shadow
                    "Regular, Shadow" -> R.layout.listvew_row_2_sans_serif_shadow
                    else -> R.layout.listvew_row_2_sans_serif
                }
            }
        }

        fun layoutIdForSansSerifCondensed(
            textStyle: String,
            center: Boolean,
            rightSource: Boolean,
        ): Int {
            if (rightSource) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_3_sans_serif_condensed_bold_right_source
                    "Bold Italic" -> R.layout.listvew_row_3_sans_serif_condensed_bold_italic_right_source
                    "Italic" -> R.layout.listvew_row_3_sans_serif_condensed_italic_right_source
                    "Italic, Shadow" -> R.layout.listvew_row_3_sans_serif_condensed_italic_shadow_right_source
                    "Regular, Shadow" -> R.layout.listvew_row_3_sans_serif_condensed_shadow_right_source
                    else -> R.layout.listvew_row_3_sans_serif_condensed_right_source
                }
            } else if (center) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_3_sans_serif_condensed_bold_center
                    "Bold Italic" -> R.layout.listvew_row_3_sans_serif_condensed_bold_italic_center
                    "Italic" -> R.layout.listvew_row_3_sans_serif_condensed_italic_center
                    "Italic, Shadow" -> R.layout.listvew_row_3_sans_serif_condensed_italic_shadow_center
                    "Regular, Shadow" -> R.layout.listvew_row_3_sans_serif_condensed_shadow_center
                    else -> R.layout.listvew_row_3_sans_serif_condensed_center
                }
            } else {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_3_sans_serif_condensed_bold
                    "Bold Italic" -> R.layout.listvew_row_3_sans_serif_condensed_bold_italic
                    "Italic" -> R.layout.listvew_row_3_sans_serif_condensed_italic
                    "Italic, Shadow" -> R.layout.listvew_row_3_sans_serif_condensed_italic_shadow
                    "Regular, Shadow" -> R.layout.listvew_row_3_sans_serif_condensed_shadow
                    else -> R.layout.listvew_row_3_sans_serif_condensed
                }
            }
        }

        fun layoutIdForSansSerifMedium(
            textStyle: String,
            center: Boolean,
            rightSource: Boolean,
        ): Int {
            if (rightSource) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_4_sans_serif_medium_bold_right_source
                    "Bold Italic" -> R.layout.listvew_row_4_sans_serif_medium_bold_italic_right_source
                    "Italic" -> R.layout.listvew_row_4_sans_serif_medium_italic_right_source
                    "Italic, Shadow" -> R.layout.listvew_row_4_sans_serif_medium_italic_shadow_right_source
                    "Regular, Shadow" -> R.layout.listvew_row_4_sans_serif_medium_shadow_right_source
                    else -> R.layout.listvew_row_4_sans_serif_medium_right_source
                }
            } else if (center) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_4_sans_serif_medium_bold_center
                    "Bold Italic" -> R.layout.listvew_row_4_sans_serif_medium_bold_italic_center
                    "Italic" -> R.layout.listvew_row_4_sans_serif_medium_italic_center
                    "Italic, Shadow" -> R.layout.listvew_row_4_sans_serif_medium_italic_shadow_center
                    "Regular, Shadow" -> R.layout.listvew_row_4_sans_serif_medium_shadow_center
                    else -> R.layout.listvew_row_4_sans_serif_medium_center
                }
            } else {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_4_sans_serif_medium_bold
                    "Bold Italic" -> R.layout.listvew_row_4_sans_serif_medium_bold_italic
                    "Italic" -> R.layout.listvew_row_4_sans_serif_medium_italic
                    "Italic, Shadow" -> R.layout.listvew_row_4_sans_serif_medium_italic_shadow
                    "Regular, Shadow" -> R.layout.listvew_row_4_sans_serif_medium_shadow
                    else -> R.layout.listvew_row_4_sans_serif_medium
                }
            }
        }

        fun layoutIdForSerif(
            textStyle: String,
            center: Boolean,
            rightSource: Boolean,
        ): Int {
            if (rightSource) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_5_serif_bold_right_source
                    "Bold Italic" -> R.layout.listvew_row_5_serif_bold_italic_right_source
                    "Italic" -> R.layout.listvew_row_5_serif_italic_right_source
                    "Italic, Shadow" -> R.layout.listvew_row_5_serif_italic_shadow_right_source
                    "Regular, Shadow" -> R.layout.listvew_row_5_serif_shadow_right_source
                    else -> R.layout.listvew_row_5_serif_right_source
                }
            } else if (center) {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_5_serif_bold_center
                    "Bold Italic" -> R.layout.listvew_row_5_serif_bold_italic_center
                    "Italic" -> R.layout.listvew_row_5_serif_italic_center
                    "Italic, Shadow" -> R.layout.listvew_row_5_serif_italic_shadow_center
                    "Regular, Shadow" -> R.layout.listvew_row_5_serif_shadow_center
                    else -> R.layout.listvew_row_5_serif_center
                }
            } else {
                return when (textStyle) {
                    "Bold" -> R.layout.listvew_row_5_serif_bold
                    "Bold Italic" -> R.layout.listvew_row_5_serif_bold_italic
                    "Italic" -> R.layout.listvew_row_5_serif_italic
                    "Italic, Shadow" -> R.layout.listvew_row_5_serif_italic_shadow
                    "Regular, Shadow" -> R.layout.listvew_row_5_serif_shadow
                    else -> R.layout.listvew_row_5_serif
                }
            }
        }
    }
}
