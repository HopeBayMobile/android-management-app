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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Aaron
 *         Created by Aaron on 2016/4/13.
 */
public class ExecutorFactory {

    public static ThreadPoolExecutor createThreadPoolExecutor() {
        int N = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                N,
                N * 2,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
        executor.allowCoreThreadTimeOut(true);
        executor.prestartCoreThread();
        return executor;
    }

}
