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

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Aaron
 *         Created by Aaron on 2016/11/3.
 */
public class ThreadPool {

    private static ThreadPoolExecutor sExecutor;

    public static ThreadPoolExecutor getInstance() {
        if (sExecutor == null) {
            synchronized (ThreadPool.class) {
                if (sExecutor == null) {
                    sExecutor = ExecutorFactory.createThreadPoolExecutor();
                }
            }
        }
        return sExecutor;
    }

}