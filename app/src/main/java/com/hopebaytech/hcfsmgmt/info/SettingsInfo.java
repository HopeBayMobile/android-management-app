package com.hopebaytech.hcfsmgmt.info;

/**
 * @author Daniel
 *         Created by Daniel on 2016/8/19.
 */
public class SettingsInfo {

    private int id;

    private boolean isEnabled;

    private String key;

    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
