package com.eyu.onequeue.push.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.eyu.onequeue.MQServerConfig;
import com.eyu.onequeue.util.PoolUtil;

public class PushMonitor implements InitializingBean {
    private final static Logger logger = LoggerFactory.getLogger(PushMonitor.class);

    private Map<String, Subscription> subs = new HashMap<>();

    private volatile boolean closed = false;

    private Thread processPushThread = new Thread(new Runnable() {

	@Override
	public void run() {
	    while (!closed) {
		Map<String, Subscription> subs = getSubs();
		for (Entry<String, Subscription> entry : subs.entrySet()) {
		    execute(() -> {
			entry.getValue().push();
		    });
		}
		try {
		    Thread.sleep(MQServerConfig.PUSH_MESSAGE_INTERVAL);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }
	}
    }, "processPushThread");

    private static ExecutorService pool = new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 6, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
	final AtomicInteger ai = new AtomicInteger(1);
	ThreadGroup group = new ThreadGroup("PushMonitor");

	@Override
	public Thread newThread(Runnable r) {
	    Thread ret = new Thread(group, r, "" + ai.getAndIncrement(), 0);
	    return ret;
	}
    });

    @Override
    public void afterPropertiesSet() throws Exception {
	processPushThread.setDaemon(true);
	processPushThread.start();
    }

    public void shutdown() {
	if (closed) {
	    return;
	}
	closed = true;
	PoolUtil.shutdown(pool, 60);
    }

    //////////////////////////////////////////////////
    private void execute(Runnable task) {
	try {
	    pool.submit(task);
	} catch (Exception e) {
	    logger.error("PushMonitor", e);
	}
    }

    private synchronized Map<String, Subscription> getSubs() {
	return new HashMap<>(this.subs);
    }

}
