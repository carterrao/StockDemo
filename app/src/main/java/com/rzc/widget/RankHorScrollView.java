package com.rzc.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by rzc on 17/11/2.
 */

public class RankHorScrollView extends HorizontalScrollView {
    private OnScrollChangedListener onScrollChangedListener;

    public RankHorScrollView(Context context) {
        super(context);
    }

    public RankHorScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollChanged(l, t);
        }
    }

    public void setOnScrollChangedListener(OnScrollChangedListener onScrollChangedListener) {
        this.onScrollChangedListener = onScrollChangedListener;
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int l, int t);
    }
}
