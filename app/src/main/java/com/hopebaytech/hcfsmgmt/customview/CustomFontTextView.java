package com.hopebaytech.hcfsmgmt.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Font;
import com.hopebaytech.hcfsmgmt.utils.HCFSMgmtUtils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/10.
 */
public class CustomFontTextView extends TextView {

    private final String CLASSNAME = getClass().getSimpleName();

    public CustomFontTextView(Context context) {
        super(context);
    }

    public CustomFontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public CustomFontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setCustomFont", "");
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
            int fontCode = typedArray.getInteger(R.styleable.CustomFont_customFont, -1);
            String fontPath = Font.getFontAssetPath(fontCode);
            try {
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
                setTypeface(typeface);
            } catch (Exception e) {
                HCFSMgmtUtils.log(Log.DEBUG, CLASSNAME, "setCustomFont", Log.getStackTraceString(e));
            }
            typedArray.recycle();
        }
    }

}
