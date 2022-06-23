package com.consumers.smppClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.type.Address;
import com.consumers.services.IMessageHandlingConsumersService;

@Service
@PropertySource("classpath:application.properties")
public class InitialSmppClient {

	@Value("${smpp.client.host}")
	private String host;
	@Value("${smpp.client.port}")
	private int port;
	@Value("${smpp.client.connect.timeout}")
	private int connectTimeout;
	@Value("${smpp.client.systemId}")
	private String systemId;
	@Value("${smpp.client.password}")
	private String password;
	@Value("${smpp.client.request.expiryTimeout}")
	private int requestExpiryTimeout;
	@Value("${smpp.client.window.monitorInterval}")
	private int windowMonitorInterval;
	@Value("${smpp.client.window.size}")
	private int windowSize;
	@Value("${smpp.client.name}")
	private String clientName;
	@Value("${smpp.client.address.range.tone}")
	private int ton;
	@Value("${smpp.client.address.range.npi}")
	private int npi;
	@Value("${smpp.client.address.range.address}")
	private String address;

	private final IMessageHandlingConsumersService messageHandlingService;

	private OutboundClient client;

	@Autowired
	public InitialSmppClient(IMessageHandlingConsumersService messageHandlingService) {
		this.messageHandlingService = messageHandlingService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public OutboundClient startSmppClient() {
		if (this.client == null) {
			this.client = createClient(messageHandlingService);
		}
		if (this.client != null && !this.client.isConnected()) {
			client.scheduleReconnect();
		}
		return client;
	}

	private OutboundClient createClient(IMessageHandlingConsumersService messageHandlingConsumersService) {
		client = new OutboundClient();
		client.initialize(getSmppSessionConfiguration(), messageHandlingConsumersService);
		return client;
	}

	public void stopSmppClient() {
		if (this.client != null && this.client.isConnected()) {
			client.shutdown();
			this.client = null;
		}
	}

	private SmppSessionConfiguration getSmppSessionConfiguration() {
		SmppSessionConfiguration config = new SmppSessionConfiguration();
		config.setWindowSize(windowSize);
		config.setName(clientName);
		config.setType(SmppBindType.TRANSMITTER);
		config.setHost(host);
		config.setPort(port);
		config.setConnectTimeout(connectTimeout);
		config.setSystemId(systemId);
		config.setPassword(password);
		config.setAddressRange(new Address((byte) ton, (byte) npi, address));
		config.getLoggingOptions().setLogBytes(true);
		config.setRequestExpiryTimeout(requestExpiryTimeout);
		config.setWindowMonitorInterval(windowMonitorInterval);
		config.setCountersEnabled(true);
		return config;
	}

	/*
	 * @EventListener(ApplicationReadyEvent.class) public SmppSession
	 * startSmppClient() { SmppSession session = null; try { ThreadPoolExecutor
	 * executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	 * ScheduledThreadPoolExecutor monitorExecutor =
	 * (ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1000, new
	 * ThreadFactory() { private AtomicInteger sequence = new AtomicInteger(0);
	 * 
	 * @Override public Thread newThread(Runnable r) { Thread t = new Thread(r);
	 * t.setName("SmppClientSessionWindowMonitorPool-" +
	 * sequence.getAndIncrement()); return t; } });
	 * 
	 * DefaultSmppClient clientBootstrap = new
	 * DefaultSmppClient(Executors.newCachedThreadPool(), 1, monitorExecutor);
	 * DefaultSmppSessionHandler sessionHandler = new
	 * ClientSmppSessionHandler(messageHandlingService); SmppSessionConfiguration
	 * config = new SmppSessionConfiguration();
	 * 
	 * config.setWindowSize(windowSize); config.setName(clientName);
	 * config.setType(SmppBindType.TRANSMITTER); config.setHost(host);
	 * config.setPort(port); config.setConnectTimeout(connectTimeout);
	 * config.setSystemId(systemId); config.setPassword(password);
	 * config.setAddressRange(new Address((byte) ton, (byte) npi, address));
	 * config.getLoggingOptions().setLogBytes(true);
	 * config.setRequestExpiryTimeout(requestExpiryTimeout);
	 * config.setWindowMonitorInterval(windowMonitorInterval);
	 * config.setCountersEnabled(true);
	 * 
	 * session = clientBootstrap.bind(config, sessionHandler);
	 * ClientSmppSessionHandler.session = session; } catch (Exception e) {
	 * log.warn("Error while start smpp client with message: " + e.getMessage()); }
	 * return session; }
	 */
}
