package com.consumers.smppClient;

import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppClient;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.type.SmppChannelConnectException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.consumers.services.IMessageHandlingConsumersService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class OutboundClient extends Client {

	private final ReconnectionDaemon reconnectionDaemon = ReconnectionDaemon.getInstance();
	private final ScheduledThreadPoolExecutor monitorExecutor;
	private final ThreadPoolExecutor executor;
	private final DefaultSmppClient clientBootstrap;
	private DefaultSmppSessionHandler sessionHandler;
	private SmppSessionConfiguration config;
	private final ScheduledExecutorService enquireLinkExecutor;
	private ScheduledFuture<?> enquireLinkTask;
//	private int enquireLinkPeriod = 60000;
	private long enquireLinkPeriod = 60L;
	private int enquireLinkTimeout = 10000;
	private boolean shutdown = false;
	private volatile Integer connectionFailedTimes = 0;

	@Value("${smpp.client.serverTimeout}")
	private final long SERVER_TIMEOUT = 30000;

	@Value("${smpp.client.deplayBeforeStart}")
	private final long DELAY_BEFORE_START = 5000;

	private boolean isApplicationStart = true;

	public OutboundClient() {
		this.enquireLinkExecutor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				String name = config.getName();
				t.setName("EnquireLink-" + name);
				return t;
			}
		});
		executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory() {
			private AtomicInteger sequence = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("SmppClientSessionWindowMonitorPool-" + sequence.getAndIncrement());
				return t;
			}
		});
		clientBootstrap = new DefaultSmppClient(executor, 1, monitorExecutor);
	}

	public void initialize(SmppSessionConfiguration config,
			IMessageHandlingConsumersService messageHandlingConsumersService) {
		try {
			this.config = config;
			sessionHandler = new ClientSmppSessionHandler(messageHandlingConsumersService);
		} catch (Exception e) {
			log.error("Error at initialize function with message: {}", e.getMessage());
		}
	}

	protected synchronized void reconnect(Integer connectionFailedTimes) {
		if (shutdown) {
			log.warn("Skipping reconnect for client {} due to shutdown", this);
			return;
		}
		// Keep reconnecting
//        if (!getConnectionFailedTimes().equals(connectionFailedTimes)) {
//            log.info("Skipping reconnect for client {} due to optimistic lock", this);
//            return;
//        }
		++this.connectionFailedTimes;
		try {
			// Sleep time is difference between multiple consumer instances to avoid
			// disconnected state on all instances.
			// Run once on application start
			if (isApplicationStart) {
				Thread.sleep(DELAY_BEFORE_START);
			}
			log.info("Connecting {}", this.config.toString());
			disconnect();
			smppSession = clientBootstrap.bind(config, sessionHandler);
			ClientSmppSessionHandler.session = smppSession;
			runEnquireLinkTask();
			this.connectionFailedTimes = 0;
			log.info("Connected {}", this.config.toString());

		} catch (SmppChannelConnectException | SmppTimeoutException e) {
			log.warn("Connection failed times: {}", this.connectionFailedTimes);
			log.error("Unable to connect: {}", e.getMessage());
			scheduleReconnect();
		} catch (Exception e) {
			log.warn("Connection failed times: {}", this.connectionFailedTimes);
			log.error("Unable to connect: {}", e.getMessage());
			scheduleReconnect();
		} finally {
			this.isApplicationStart = false;
		}
	}

	public void scheduleReconnect() {
		try {
			reconnectionDaemon.scheduleReconnect(this, connectionFailedTimes, createReconnectionTask());
		} catch (Exception e) {
			log.error("Error at scheduleReconnect function with message: {}", e.getMessage());
		}
	}

	private ReconnectionTask createReconnectionTask() {
		return new ReconnectionTask(this, connectionFailedTimes);
	}

	private void runEnquireLinkTask() {
		enquireLinkTask = this.enquireLinkExecutor.scheduleWithFixedDelay(new EnquireLinkTask(this, enquireLinkTimeout),
				enquireLinkPeriod, enquireLinkPeriod, TimeUnit.SECONDS);
	}

	public synchronized void shutdown() {
		log.info("Shutting down client {}", this);
		try {
			shutdown = true;
			disconnect();
			clientBootstrap.destroy();
			executor.shutdownNow();
			enquireLinkExecutor.shutdownNow();
			monitorExecutor.shutdownNow();
		} catch (Exception e) {
			log.error("Error at shutdown function with message: {}", e.getMessage());
		}
	}

	private void disconnect() {
		stopEnquireLinkTask();
		destroySession();
	}

	private void stopEnquireLinkTask() {
		try {
			if (enquireLinkTask != null) {
				this.enquireLinkTask.cancel(true);
			}
		} catch (Exception e) {
			log.error("Error at stopEnquireLinkTask function with message: {}", e.getMessage());
		}
	}

	private void destroySession() {
		try {
			if (smppSession != null) {
				log.debug("Cleaning up session... (final counters)");
				logCounters();
				smppSession.destroy();
				smppSession = null;
			}
		} catch (Exception e) {
			log.error("Error at destroySession function with error: {}", e.getMessage());
		}
	}

	private void logCounters() {
		if (smppSession.hasCounters()) {
			log.debug("tx-enquireLink: {}", smppSession.getCounters().getTxEnquireLink());
			log.debug("tx-submitSM: {}", smppSession.getCounters().getTxSubmitSM());
			log.debug("tx-deliverSM: {}", smppSession.getCounters().getTxDeliverSM());
			log.debug("tx-dataSM: {}", smppSession.getCounters().getTxDataSM());
			log.debug("rx-enquireLink: {}", smppSession.getCounters().getRxEnquireLink());
			log.debug("rx-submitSM: {}", smppSession.getCounters().getRxSubmitSM());
			log.debug("rx-deliverSM: {}", smppSession.getCounters().getRxDeliverSM());
			log.debug("rx-dataSM: {}", smppSession.getCounters().getRxDataSM());
		}
	}

	public Integer getConnectionFailedTimes() {
		return connectionFailedTimes;
	}

	@Override
	public SmppSessionConfiguration getConfiguration() {
		return config;
	}

}
