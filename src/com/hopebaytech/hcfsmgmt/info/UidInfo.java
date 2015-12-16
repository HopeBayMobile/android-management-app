package com.hopebaytech.hcfsmgmt.info;

public class UidInfo {

	private int uid;
	private String packageName;
	
	public UidInfo() {
		
	}
	
	public UidInfo(int uid, String packageName) {
		this.uid = uid;
		this.packageName = packageName;
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String pkgName) {
		this.packageName = pkgName;
	}

}