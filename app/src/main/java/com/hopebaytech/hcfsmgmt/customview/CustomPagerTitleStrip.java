package com.hopebaytech.hcfsmgmt.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.FloatRange;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.PagerTitleStrip;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Font;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/10.
 */
public class CustomPagerTitleStrip extends PagerTitleStrip {

    private final String CLASSNAME = CustomPagerTitleStrip.class.getSimpleName();

    public CustomPagerTitleStrip(Context context) {
        super(context);
    }

    public CustomPagerTitleStrip(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (isInEditMode()) {
            return;
        }

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Typeface typeFace = getCustomTypeFace(context, attrs);
        if (typeFace == null) {
            return;
        }

        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            if (view instanceof TextView) {
                ((TextView) view).setTypeface(typeFace);
            }
        }

        setNonPrimaryAlpha(0.3f);
    }

    @Nullable
    private Typeface getCustomTypeFace(Context context, AttributeSet attrs) {
        Logs.d(CLASSNAME, "setCustomFont", "");

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
        int fontCode = typedArray.getInteger(R.styleable.CustomFont_customFont, 0);
        String fontPath = Font.getFontAssetPath(fontCode);
        Typeface typeface = null;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
        } catch (Exception e) {
            Logs.d(CLASSNAME, "setCustomFont", Log.getStackTraceString(e));
        }
        typedArray.recycle();

        return typeface;
    }

}
