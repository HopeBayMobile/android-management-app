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
package com.hopebaytech.hcfsmgmt.utils;

/**
 * @author Aaron
 *         Created by Aaron on 2016/8/30.
 */
public class RestoreStatus {

    public static final int NONE = 0;
    public static final int MINI_RESTORE_IN_PROGRESS = 1;
    public static final int MINI_RESTORE_COMPLETED = 2;
    public static final int FULL_RESTORE_IN_PROGRESS = 3;
    public static final int FULL_RESTORE_COMPLETED = 4;

    public static class Error {
        public static final int DAMAGED_BACKUP = HCFSEvent.ErrorCode.ENOENT;
        public static final int OUT_OF_SPACE = HCFSEvent.ErrorCode.ENOSPC;
        public static final int CONN_FAILED = HCFSEvent.ErrorCode.ENETDOWN;
    }

}
