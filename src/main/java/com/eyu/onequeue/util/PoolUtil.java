package com.eyu.onequeue.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PoolUtil {
    private static final Logger logger = LoggerFactory.getLogger(PoolUtil.class);

    public static void shutdown(ExecutorService pool, long awaitTime) {
	pool.shutdown();
	logger.error("开始关闭总线线程池");
	try {
	    if (!pool.awaitTermination(awaitTime, TimeUnit.SECONDS)) {
		logger.error("无法在预计时间内完成事件总线线程池关闭,尝试强行关闭");
		pool.shutdownNow();
		if (!pool.awaitTermination(awaitTime, TimeUnit.SECONDS)) {
		    logger.error("总线线程池无法完成关闭");
		}
	    }
	} catch (InterruptedException e) {
	    logger.error("总线线程池关闭时线程被打断,强制关闭事件总线线程池");
	    pool.shutdownNow();
	}
    }
}
