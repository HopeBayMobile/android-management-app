package com.hopebaytech.hcfsmgmt.utils;

public class HCFSApiUtils {
	
	static {
		System.loadLibrary("HCFSAPI");
	}
	
	public static native String helloJNI(String str);
	public static native String is_login();
	public static native String ping();
	public static native String unpin();
	public static native String unpin_list();
	public static native String total_storage();
	public static native String avail_storage();
	public static native String used_storage();
	public static native void start_sync_to_cloud();
	public static native void stop_sync_to_cloud();
	
}
