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
        ThreadPoolExecutor executor = new ThreadPoolExecutor(N, N * 2, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        executor.allowCoreThreadTimeOut(true);
        executor.prestartCoreThread();
        return executor;
    }

}
