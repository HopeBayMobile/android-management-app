package com.hopebaytech.hcfsmgmt.utils;

public class HCFSApiUtils {
	
	static {
		System.loadLibrary("HCFSAPI");
	}
	
	public static native String helloJNI(String str);
	public static native String is_login();
	public static native String pin(String filePath);
	public static native String unpin(String filePath);
	public static native String unpin_list();
	public static native String total_storage();
	public static native String avail_storage();
	public static native String used_storage();
	public static native void start_sync_to_cloud();
	public static native void stop_sync_to_cloud();
	public static native boolean get_pin_status(String filePath);
	
}
