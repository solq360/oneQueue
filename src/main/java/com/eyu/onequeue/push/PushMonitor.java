package com.eyu.onequeue.push;

import java.io.File;
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
import org.springframework.stereotype.Service;

import com.eyu.onequeue.MQServerConfig;
import com.eyu.onequeue.socket.model.QNode;
import com.eyu.onequeue.util.PoolUtil;
import com.eyu.onequeue.util.SerialUtil;
import com.fasterxml.jackson.core.type.TypeReference;

@Service
public class PushMonitor implements InitializingBean {
	private final static Logger logger = LoggerFactory.getLogger(PushMonitor.class);

	private Map<String, Subscribe> subs = new HashMap<>();

	private volatile boolean closed = false;
	private long lastPersistRecord = System.currentTimeMillis();

	private Thread processPushThread = new Thread(new Runnable() {

		@Override
		public void run() {
			while (!closed) {
				Map<String, Subscribe> subs = getSubs();
				for (Entry<String, Subscribe> entry : subs.entrySet()) {
					execute(() -> {
						Subscribe sub = entry.getValue();
						synchronized (sub) {
							sub.push();
						}
					});
				}

				if ((System.currentTimeMillis() - lastPersistRecord) > MQServerConfig.PUSH_PERSIST_INTERVAL) {
					lastPersistRecord = System.currentTimeMillis();
					execute(() -> persist());
				}

				try {
					Thread.sleep(MQServerConfig.PUSH_MESSAGE_INTERVAL);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}, "processPushThread");

	private static ExecutorService pool = new ThreadPoolExecutor(4, Runtime.getRuntime().availableProcessors() * 6, 1,
			TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
				final AtomicInteger ai = new AtomicInteger(1);
				ThreadGroup group = new ThreadGroup("PushMonitor");

				@Override
				public Thread newThread(Runnable r) {
					Thread ret = new Thread(group, r, "" + ai.getAndIncrement(), 0);
					return ret;
				}
			});

	public void registerNode(String topic, String groupId, QNode node) {
		Subscribe sub = getSub(topic, groupId);
		synchronized (sub) {
			sub.addNode(node);
		}
	}

	public void removeNode(String topic, String groupId, QNode node) {
		Subscribe sub = getSub(topic, groupId);
		synchronized (sub) {
			sub.removeNode(node);
		}
	}

	public void removeNode(QNode node) {
		Map<String, Subscribe> subs = getSubs();
		for (Subscribe sub : subs.values()) {
			synchronized (sub) {
				sub.removeNode(node);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		processPushThread.setDaemon(true);
		processPushThread.start();

		String thisFileName = MQServerConfig.PUSH_PERSIST_FILE;
		if (new File(thisFileName).exists()) {
			subs = SerialUtil.readValueAsFile(thisFileName, new TypeReference<HashMap<String, Subscribe>>() {
			});
		}
	}

	public void shutdown() {
		if (closed) {
			return;
		}
		closed = true;
		PoolUtil.shutdown(pool, 60);
		persist();
	}

	//////////////////////////////////////////////////
	private synchronized void persist() {
		String thisFileName = MQServerConfig.PUSH_PERSIST_FILE;
		SerialUtil.writeValueAsFile(thisFileName, subs);
	}

	private Subscribe getSub(String topic, String groupId) {
		final String key = topic + ":" + groupId;
		Subscribe ret = subs.get(key);
		if (ret != null) {
			return ret;
		}
		synchronized (this) {
			ret = subs.get(groupId);
			if (ret != null) {
				return ret;
			}
			ret = Subscribe.of(topic, groupId);
			subs.put(key, ret);
		}
		return ret;
	}

	private void execute(Runnable task) {
		try {
			pool.submit(task);
		} catch (Exception e) {
			logger.error("PushMonitor", e);
		}
	}

	private synchronized Map<String, Subscribe> getSubs() {
		return new HashMap<>(this.subs);
	}

}
