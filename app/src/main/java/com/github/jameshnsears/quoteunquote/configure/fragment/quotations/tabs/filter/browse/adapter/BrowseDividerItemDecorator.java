package com.github.jameshnsears.quoteunquote.configure.fragment.quotations.tabs.filter.browse.adapter;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class BrowseDividerItemDecorator extends RecyclerView.ItemDecoration {
    private final Drawable mDivider;

    public BrowseDividerItemDecorator(final Drawable divider) {
        this.mDivider = divider;
    }

    @Override
    public void onDrawOver(final Canvas canvas, final RecyclerView parent, final RecyclerView.State state) {
        final int dividerLeft = parent.getPaddingLeft();
        final int dividerRight = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i <= childCount - 2; i++) {
            final View child = parent.getChildAt(i);

            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            final int dividerTop = child.getBottom() + params.bottomMargin;
            final int dividerBottom = dividerTop + this.mDivider.getIntrinsicHeight();

            this.mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            this.mDivider.draw(canvas);
        }
    }
}
