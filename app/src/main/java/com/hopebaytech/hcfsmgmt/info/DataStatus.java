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

import com.hopebaytech.hcfsmgmt.utils.HCFSConnStatus;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/25.
 */
public class DataStatus {

    public static final int UNKNOWN = -1;

    /**
     * <p>The data of file/app is all on the device. Or, the tera connection status is one of the
     * following status:
     * <li>{@link HCFSConnStatus#TRANS_NORMAL}</li>
     * <li>{@link HCFSConnStatus#TRANS_IN_PROGRESS}</li>
     * <li>{@link HCFSConnStatus#TRANS_SLOW}</li>
     * </p>
     */
    public static final int AVAILABLE = 0;

    /**
     * <p>The data of file/app is NOT all on the device. Or, the tera connection status is one of the
     * following status:
     * <li>{@link HCFSConnStatus#TRANS_FAILED}</li>
     * <li>{@link HCFSConnStatus#TRANS_NOT_ALLOWED}</li>
     * </p>
     */
    public static final int UNAVAILABLE = 1;

}
