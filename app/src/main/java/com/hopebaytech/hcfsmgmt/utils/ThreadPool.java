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