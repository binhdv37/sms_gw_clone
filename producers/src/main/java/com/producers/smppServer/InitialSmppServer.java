package com.producers.smppServer;

import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.producers.services.IMessageHandlingProducersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@PropertySource("classpath:application.properties")
public class InitialSmppServer {

	@Value("${smppServer.port}")
	private int port;
	
	@Value("${smppServer.systemId}")
	private String systemId;
	
	@Value("${smppServer.maxConnection}")
	private int maxConnection;
	
	@Value("${smppServer.corePoolSize}")
	private int corePoolSize;
	
	@Value("${smppServer.defaultRequestExpiryTimeout}")
	private int defaultRequestExpiryTimeout;
	
	@Value("${smppServer.defaultWindowMonitorInterval}")
	private int defaultWindowMonitorInterval;
	
	@Value("${smppServer.defaultWindowSize}")
	private int defaultWindowSize;
	
	@Value("${smppServer.nonBlockingSocketsEnabled}")
	private boolean nonBlockingSocketsEnabled;
	
	@Value("${smppServer.defaultSessionCountersEnabled}")
	private boolean defaultSessionCountersEnabled;
	
	@Value("${smppServer.jmxEnabled}")
	private boolean jmxEnabled;

	private final IMessageHandlingProducersService directMessage;

	private DefaultSmppServer smppServer;

	@Autowired
	public InitialSmppServer(IMessageHandlingProducersService directMessage) {
		this.directMessage = directMessage;
	}

	private void initSmppServer() {
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors
				.newScheduledThreadPool(corePoolSize, new ThreadFactory() {
					private AtomicInteger sequence = new AtomicInteger(0);

					@Override
					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(runnable);
						thread.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
						return thread;
					}
				});

		SmppServerConfiguration configuration = new SmppServerConfiguration();
		configuration.setPort(port);
		configuration.setSystemId(systemId);
		configuration.setMaxConnectionSize(maxConnection);
		configuration.setNonBlockingSocketsEnabled(nonBlockingSocketsEnabled);
		configuration.setDefaultRequestExpiryTimeout(defaultRequestExpiryTimeout);
		configuration.setDefaultWindowMonitorInterval(defaultWindowMonitorInterval);
		configuration.setDefaultWindowSize(defaultWindowSize);
		configuration.setDefaultWindowWaitTimeout(configuration.getDefaultRequestExpiryTimeout());
		configuration.setDefaultSessionCountersEnabled(defaultSessionCountersEnabled);
		configuration.setJmxEnabled(jmxEnabled);

		this.smppServer = new DefaultSmppServer(configuration, new DftSmppServerHandler(directMessage), executor,
				monitorExecutor);

	}

	@EventListener(ApplicationReadyEvent.class)
	public void startSmppServer() {
		try {
			if (this.smppServer == null) {
				initSmppServer();
			}
			if (this.smppServer != null && !this.smppServer.isStarted()) {
				log.info("Starting SMPP server...");
				smppServer.start();
				log.info("SMPP server started");
			}
		} catch (SmppChannelException ex) {
			log.error("Error while start SMSC with message: {}", ex.getMessage());
		}
	}

	public void stopSmppServer() {
		if (this.smppServer != null && !this.smppServer.isStopped()) {
			log.info("Stopping SMPP server...");
			this.smppServer.stop();
			log.info("Server counters: {}", smppServer.getCounters());
		}
	}
}
