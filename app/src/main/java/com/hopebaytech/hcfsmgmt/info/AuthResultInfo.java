package com.hopebaytech.hcfsmgmt.info;

/**
 * Created by Aaron on 2016/3/8.
 */
public class AuthResultInfo {

    private String backend_type;
    private String account;
    private String user;
    private String password;
    private String backend_url;
    private String bucket;
    private String protocol;

    public String getBackendType() {
        return backend_type;
    }

    public void setBackendType(String backend_type) {
        this.backend_type = backend_type;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBackendUrl() {
        return backend_url;
    }

    public void setBackendUrl(String backend_url) {
        this.backend_url = backend_url;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
