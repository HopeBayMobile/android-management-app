package com.hopebaytech.hcfsmgmt.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerTabStrip;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Font;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/10.
 */
public class CustomPagerTabStrip extends PagerTabStrip {

    private final String CLASSNAME = getClass().getSimpleName();

    public CustomPagerTabStrip(Context context) {
        super(context);
    }

    public CustomPagerTabStrip(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            Typeface tf = getCustomTypeFace(context, attrs);
            if (tf != null) {
                for (int i = 0; i < getChildCount(); i++) {
                    View view = getChildAt(i);
                    if (view instanceof TextView) {
                        ((TextView) view).setTypeface(tf);
                    }
                }
            }
        }
    }

    @Nullable
    private Typeface getCustomTypeFace(Context context, AttributeSet attrs) {
        HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setCustomFont", "");

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
        int fontCode = typedArray.getInteger(R.styleable.CustomFont_customFont, 0);
        String fontPath = Font.getFontAssetPath(fontCode);
        Typeface typeface = null;
        try {
            typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
        } catch (Exception e) {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setCustomFont", Log.getStackTraceString(e));
        }
        typedArray.recycle();

        return typeface;
    }

}
