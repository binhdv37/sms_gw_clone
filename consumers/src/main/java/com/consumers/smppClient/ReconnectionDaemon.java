package com.consumers.smppClient;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ReconnectionDaemon {

	private static final ReconnectionDaemon RECONNECTION_DAEMON = new ReconnectionDaemon("0,5,15");
	private static final long KEEP_ALIVE_TIME = 60L;
	private final String[] reconnectionPeriods;
	private final ThreadPoolExecutor executor;
	private final ScheduledExecutorService scheduledExecutorService;

	public ReconnectionDaemon(String reconnectionPeriods) {
		this.reconnectionPeriods = reconnectionPeriods.split(",");
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(getThreadFactory("ReconnectionSchedulerDaemon-"));

		executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), getThreadFactory("ReconnectionExecutorDaemon-"));
	}

	private ThreadFactory getThreadFactory(final String name) {
		return new ThreadFactory() {
			private AtomicInteger sequence = new AtomicInteger(0);
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName(name + sequence.getAndIncrement());
				return t;
			}
		};
	}

	public static ReconnectionDaemon getInstance() {
		return RECONNECTION_DAEMON;
	}

	public void scheduleReconnect(OutboundClient outboundClient, Integer failureCount, ReconnectionTask reconnectionTask) {
		try {
			long delay = getReconnectionPeriod(failureCount);
			log.info("Scheduling reconnect for {} in {} seconds", outboundClient.getConfiguration().toString(), delay);
			scheduledExecutorService.schedule(new ScheduledTask(reconnectionTask), delay, TimeUnit.SECONDS);
		} catch (Exception e) {
			log.error("Error at scheduleReconnect function with message: {}", e.getMessage());
		}
	}

	private long getReconnectionPeriod(Integer failureCount) {
		String reconnectionPeriod;
		if (reconnectionPeriods.length > failureCount) {
			reconnectionPeriod = reconnectionPeriods[failureCount];
		} else {
			reconnectionPeriod = reconnectionPeriods[reconnectionPeriods.length - 1];
		}
		return Long.parseLong(reconnectionPeriod);
	}

	@PreDestroy
	public void shutdown() {
		try {
			executor.shutdown();
			scheduledExecutorService.shutdown();
		} catch (Exception e) {
			log.error("Error at shutdown function with message: {}", e.getMessage());
		}
	}

	private class ScheduledTask implements Runnable {

		private final Runnable task;

		public ScheduledTask(Runnable task) {
			this.task = task;
		}

		@Override
		public void run() {
			executor.execute(task);
		}
	}
}
