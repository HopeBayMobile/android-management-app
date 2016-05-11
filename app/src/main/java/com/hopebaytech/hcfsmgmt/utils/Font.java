package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/5/10.
 */
public class Font {

    private static final int OPEN_SANS_LIGHT = 0;
    private static final int OPEN_SANS_REGULAR = 1;
    private static final int OPEN_SANS_SEMIBOLD = 2;
    private static final int OPEN_SANS_BOLD = 3;
    private static final int NOTO_SANS_TC_LIGHT = 4;
    private static final int NOTO_SANS_TC_REGULAR = 5;
    private static final int NOTO_SANS_TC_MEDIUM = 6;
    private static final int NOTO_SANS_TC_BOLD = 7;
    private static final int NOTO_SANS_KR_LIGHT = 8;
    private static final int NOTO_SANS_KR_REGULAR = 9;
    private static final int NOTO_SANS_KR_MEDIUM = 10;
    private static final int NOTO_SANS_KR_BOLD = 11;

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
