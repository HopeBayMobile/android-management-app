/*
 * Copyright (c) 2021 HopeBayTech.
 *
 * This file is part of Tera.
 * See https://github.com/HopeBayMobile for further info.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hopebaytech.hcfsmgmt.info;

import java.util.ArrayList;

public class ServiceAppInfo {

	private boolean isPinned;
	private String appName;
	private String packageName;
	private String sourceDir;
	private String dataDir;
//	private String externalDir;
	private ArrayList<String> externalDirList;

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

//	public String getExternalDir() {
//		return externalDir;
//	}
//
//	public void setExternalDir(String externalDir) {
//		this.externalDir = externalDir;
//	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public ArrayList<String> getExternalDirList() {
		return externalDirList;
	}

	public void setExternalDirList(ArrayList<String> externalDirList) {
		this.externalDirList = externalDirList;
	}
}