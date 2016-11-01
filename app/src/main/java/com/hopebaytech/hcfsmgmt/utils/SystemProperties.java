package com.hopebaytech.hcfsmgmt.utils;

import java.lang.reflect.Method;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/1.
 */

public class SystemProperties {

    public static String get(String key) {
        String value = "";
        try {
            Class<?> _class = Class.forName("android.os.SystemProperties");
            Method method = _class.getDeclaredMethod("get", String.class);
            value = (String) method.invoke(_class, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
