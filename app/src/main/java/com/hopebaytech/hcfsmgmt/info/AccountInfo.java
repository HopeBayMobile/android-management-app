package com.hopebaytech.hcfsmgmt.info;

/**
 * Created by Aaron on 2016/4/19.
 */
public class AccountInfo {

    private int id;
    private String name;
    private String email;
    private String imgUrl;
    private String imgBase64;
    private long imgExpringTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getImgBase64() {
        return imgBase64;
    }

    public void setImgBase64(String imgBase64) {
        this.imgBase64 = imgBase64;
    }

    public long getImgExpringTime() {
        return imgExpringTime;
    }

    public void setImgExpringTime(long imgExpringTime) {
        this.imgExpringTime = imgExpringTime;
    }
}
