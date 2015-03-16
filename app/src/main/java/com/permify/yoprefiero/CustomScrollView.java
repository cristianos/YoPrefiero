package com.permify.yoprefiero;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Created by Idata on 25-02-2015.
 */
public class CustomScrollView extends ScrollView {

    public CustomScrollView(Context context) {
        super(context);
    }

    public CustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getSolidColor() {

        //return R.color.amarillo_prefiero;
        return Color.rgb(255, 255, 255);
    }
}
