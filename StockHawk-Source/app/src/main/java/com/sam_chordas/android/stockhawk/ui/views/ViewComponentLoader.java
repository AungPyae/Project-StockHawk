package com.sam_chordas.android.stockhawk.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by aungpyae on 24/3/15.
 */
public class ViewComponentLoader extends FrameLayout{

    public ViewComponentLoader(Context context) {
        super(context);
    }

    public ViewComponentLoader(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ViewComponentLoader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void displayLoader(){
        setVisibility(View.VISIBLE);
    }

    public void dismissLoader(){
        setVisibility(View.GONE);
    }
}
