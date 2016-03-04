package com.hopebaytech.hcfsmgmt.info;

public class ServiceAppInfo {

	private boolean isPinned;
	private String appName;
	private String packageName;
	private String sourceDir;
	private String dataDir;
	private String externalDir;

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public boolean isPinned() {
		return isPinned;
	}

	public void setPinned(boolean isPinned) {
		this.isPinned = isPinned;
	}

	public String getSourceDir() {
		return sourceDir;
	}

	public void setSourceDir(String sourceDir) {
		this.sourceDir = sourceDir;
	}

	public String getDataDir() {
		return dataDir;
	}

	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	public String getExternalDir() {
		return externalDir;
	}

	public void setExternalDir(String externalDir) {
		this.externalDir = externalDir;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}