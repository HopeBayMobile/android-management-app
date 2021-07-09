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
package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/10.
 */
public class Font {

    public static final int OPEN_SANS_LIGHT = 0;
    public static final int OPEN_SANS_REGULAR = 1;
    public static final int OPEN_SANS_SEMIBOLD = 2;
    public static final int OPEN_SANS_BOLD = 3;
    public static final int NOTO_SANS_TC_LIGHT = 4;
    public static final int NOTO_SANS_TC_REGULAR = 5;
    public static final int NOTO_SANS_TC_MEDIUM = 6;
    public static final int NOTO_SANS_TC_BOLD = 7;
    public static final int NOTO_SANS_KR_LIGHT = 8;
    public static final int NOTO_SANS_KR_REGULAR = 9;
    public static final int NOTO_SANS_KR_MEDIUM = 10;
    public static final int NOTO_SANS_KR_BOLD = 11;

    public static String getFontAssetPath(int fontCode) {
        String fontPath;
        switch (fontCode) {
            case Font.OPEN_SANS_LIGHT:
                fontPath = "fonts/OpenSans-Light.ttf";
                break;
            case Font.OPEN_SANS_REGULAR:
                fontPath = "fonts/OpenSans-Regular.ttf";
                break;
            case Font.OPEN_SANS_SEMIBOLD:
                fontPath = "fonts/OpenSans-Semibold.ttf";
                break;
            case Font.OPEN_SANS_BOLD:
                fontPath = "fonts/OpenSans-Bold.ttf";
                break;
            default:
                fontPath = "fonts/OpenSans-Regular.ttf";
                break;
        }
        return fontPath;
    }

}
