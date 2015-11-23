package com.hopebaytech.hcfsmgmt.utils;

public class HCFSApiUtils {
	
	static {
		System.loadLibrary("HCFSAPI");
	}
	
	public static native String getFileStatus(String filePath);
	public static native String getHCFSConfig(String key);
	public static native String getHCFSProperty(String key);
	public static native String pin(String pinPath);
	public static native String getPinStatus(String filePath);
	public static native String setHCFSConfig(String key, String value);
	public static native String setHCFSProperty(String key, String value);
	public static native String getHCFSStat();
	public static native String unpin(String unpinPath);
	
	public static native String helloJNI(String str);
	public static native String is_login();
	public static native String used_storage();
	
}
