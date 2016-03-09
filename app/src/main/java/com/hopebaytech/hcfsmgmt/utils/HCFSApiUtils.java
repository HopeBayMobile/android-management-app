package com.hopebaytech.hcfsmgmt.utils;

public class HCFSApiUtils {
	
	static {
		System.loadLibrary("terafonnapi");
	}
	
	public static native String getFileStatus(String filePath);
	public static native String getDirStatus(String filePath);
	public static native String getHCFSConfig(String key);
//	public static native String getHCFSProperty(String key);
//	public static native String setHCFSProperty(String key, String value);
	public static native String pin(String pinPath);
	public static native String getPinStatus(String filePath);
	public static native String setHCFSConfig(String key, String value);
	public static native String getHCFSStat();
	public static native String unpin(String unpinPath);
	public static native String reboot();
	public static native String reloadConfig();
	public static native String resetXfer();
	public static native String setHCFSSyncStatus(int enabled);
	public static native String getHCFSSyncStatus();
	public static native String getEncryptedIMEI();

}
