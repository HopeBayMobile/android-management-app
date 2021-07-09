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
 * @author Vince
 *         Created by Vince on 2016/7/19.
 */
public class HCFSEvent {

    public static final int TEST = 0;
    public static final int TOKEN_EXPIRED = 1;
    public static final int UPLOAD_COMPLETED = 2;
    public static final int RESTORE_STAGE_1 = 3;
    public static final int RESTORE_STAGE_2 = 4;
    public static final int CREATE_THUMBNAIL = 7;
    public static final int BOOSTER_PROCESS_COMPLETED = 8;
    public static final int BOOSTER_PROCESS_FAILED = 9;

    // The following event is not implemented.
    public static final int EXCEED_PIN_MAX = 5;
    public static final int SPACE_NOT_ENOUGH = 6;

    public static class ErrorCode {

        /**
         * No such file or directory.
         */
        public static final int ENOENT = -2;

        /**
         * No space left on device.
         */
        public static final int ENOSPC = -28;

        /**
         * Network is down.
         */
        public static final int ENETDOWN = -100;

    }

}
