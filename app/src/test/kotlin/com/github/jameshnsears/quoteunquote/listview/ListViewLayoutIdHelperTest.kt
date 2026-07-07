package com.github.jameshnsears.quoteunquote.listview

import com.github.jameshnsears.quoteunquote.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class ListViewLayoutIdHelperTest {
    @Test
    fun layoutIdForCursive() {
        // rightSource = true
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Bold", center = false, rightSource = true),
            `is`(R.layout.listvew_row_0_cursive_bold_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Bold Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_0_cursive_bold_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_0_cursive_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Italic, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_0_cursive_italic_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Regular, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_0_cursive_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Regular", center = false, rightSource = true),
            `is`(R.layout.listvew_row_0_cursive_right_source),
        )

        // center = true, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Bold", center = true, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_bold_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Bold Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_bold_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Italic, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_italic_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Regular, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Regular", center = true, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_center),
        )

        // center = false, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Bold", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_bold),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Bold Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_bold_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Italic, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_italic_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Regular, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Regular", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForCursive("Unknown", center = false, rightSource = false),
            `is`(R.layout.listvew_row_0_cursive),
        )
    }

    @Test
    fun layoutIdForMonospace() {
        // rightSource = true
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Bold", center = false, rightSource = true),
            `is`(R.layout.listvew_row_1_monospace_bold_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Bold Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_1_monospace_bold_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_1_monospace_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Italic, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_1_monospace_italic_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Regular, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_1_monospace_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Regular", center = false, rightSource = true),
            `is`(R.layout.listvew_row_1_monospace_right_source),
        )

        // center = true, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Bold", center = true, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_bold_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Bold Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_bold_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Italic, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_italic_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Regular, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Regular", center = true, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_center),
        )

        // center = false, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Bold", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_bold),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Bold Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_bold_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Italic, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_italic_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Regular, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Regular", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForMonospace("Unknown", center = false, rightSource = false),
            `is`(R.layout.listvew_row_1_monospace),
        )
    }

    @Test
    fun layoutIdForSansSerif() {
        // rightSource = true
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Bold", center = false, rightSource = true),
            `is`(R.layout.listvew_row_2_sans_serif_bold_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Bold Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_2_sans_serif_bold_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_2_sans_serif_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Italic, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_2_sans_serif_italic_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Regular, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_2_sans_serif_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Regular", center = false, rightSource = true),
            `is`(R.layout.listvew_row_2_sans_serif_right_source),
        )

        // center = true, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Bold", center = true, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_bold_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Bold Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_bold_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Italic, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_italic_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Regular, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Regular", center = true, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_center),
        )

        // center = false, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Bold", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_bold),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Bold Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_bold_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Italic, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_italic_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Regular, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Regular", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerif("Unknown", center = false, rightSource = false),
            `is`(R.layout.listvew_row_2_sans_serif),
        )
    }

    @Test
    fun layoutIdForSansSerifCondensed() {
        // rightSource = true
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Bold", center = false, rightSource = true),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_bold_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Bold Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_bold_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Italic, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_italic_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Regular, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Regular", center = false, rightSource = true),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_right_source),
        )

        // center = true, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Bold", center = true, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_bold_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Bold Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_bold_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Italic, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_italic_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Regular, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Regular", center = true, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_center),
        )

        // center = false, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Bold", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_bold),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Bold Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_bold_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Italic, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_italic_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Regular, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Regular", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifCondensed("Unknown", center = false, rightSource = false),
            `is`(R.layout.listvew_row_3_sans_serif_condensed),
        )
    }

    @Test
    fun layoutIdForSansSerifMedium() {
        // rightSource = true
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Bold", center = false, rightSource = true),
            `is`(R.layout.listvew_row_4_sans_serif_medium_bold_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Bold Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_4_sans_serif_medium_bold_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_4_sans_serif_medium_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Italic, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_4_sans_serif_medium_italic_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Regular, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_4_sans_serif_medium_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Regular", center = false, rightSource = true),
            `is`(R.layout.listvew_row_4_sans_serif_medium_right_source),
        )

        // center = true, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Bold", center = true, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_bold_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Bold Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_bold_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Italic, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_italic_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Regular, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Regular", center = true, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_center),
        )

        // center = false, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Bold", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_bold),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Bold Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_bold_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Italic, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_italic_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Regular, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Regular", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSansSerifMedium("Unknown", center = false, rightSource = false),
            `is`(R.layout.listvew_row_4_sans_serif_medium),
        )
    }

    @Test
    fun layoutIdForSerif() {
        // rightSource = true
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Bold", center = false, rightSource = true),
            `is`(R.layout.listvew_row_5_serif_bold_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Bold Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_5_serif_bold_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Italic", center = false, rightSource = true),
            `is`(R.layout.listvew_row_5_serif_italic_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Italic, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_5_serif_italic_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Regular, Shadow", center = false, rightSource = true),
            `is`(R.layout.listvew_row_5_serif_shadow_right_source),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Regular", center = false, rightSource = true),
            `is`(R.layout.listvew_row_5_serif_right_source),
        )

        // center = true, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Bold", center = true, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_bold_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Bold Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_bold_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Italic", center = true, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_italic_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Italic, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_italic_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Regular, Shadow", center = true, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_shadow_center),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Regular", center = true, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_center),
        )

        // center = false, rightSource = false
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Bold", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_bold),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Bold Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_bold_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Italic", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_italic),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Italic, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_italic_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Regular, Shadow", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif_shadow),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Regular", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif),
        )
        assertThat(
            ListViewLayoutIdHelper.layoutIdForSerif("Unknown", center = false, rightSource = false),
            `is`(R.layout.listvew_row_5_serif),
        )
    }
}
