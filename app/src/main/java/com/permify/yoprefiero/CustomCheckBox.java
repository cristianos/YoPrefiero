package com.permify.yoprefiero;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

/**
 * http://danielme.com/2013/10/09/diseno-android-listview-con-checkbox/
 */
public class CustomCheckBox extends CheckBox {

    public CustomCheckBox(Context context){
        super(context);
    }

    public CustomCheckBox(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public CustomCheckBox(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
    }

    @Override
    public void setPressed(boolean pressed){
        if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()){
            return;
        }

        super.setPressed(pressed);
    }
}
