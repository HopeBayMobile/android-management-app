/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.hopebaytech.hcfsmgmt.R;
import com.hopebaytech.hcfsmgmt.utils.Font;
import com.hopebaytech.hcfsmgmt.utils.Logs;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/10.
 */
public class FontTextView extends TextView {

    private final String CLASSNAME = getClass().getSimpleName();

    public FontTextView(Context context) {
        super(context);
    }

    public FontTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public FontTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(Context context, AttributeSet attrs) {
        if (!isInEditMode()) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
            int fontCode = typedArray.getInteger(R.styleable.CustomFont_customFont, -1);
            String fontPath = Font.getFontAssetPath(fontCode);
            try {
                Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontPath);
                setTypeface(typeface);
            } catch (Exception e) {
                Logs.d(CLASSNAME, "setCustomFont", Log.getStackTraceString(e));
            }
            typedArray.recycle();
        }
    }

}
