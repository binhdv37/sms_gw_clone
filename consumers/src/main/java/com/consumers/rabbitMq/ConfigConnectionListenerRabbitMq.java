package com.consumers.rabbitMq;

import java.io.IOException;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.consumers.smppClient.ClientSmppSessionHandler;
import com.consumers.smppClient.InitialSmppClient;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ConfigConnectionListenerRabbitMq {

	@Autowired
	private InitialSmppClient initialSmppClient;

	@Bean
	public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory,
			SimpleRabbitListenerContainerFactoryConfigurer configurer) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		connectionFactory.addConnectionListener(new ConnectionListener() {

			@Override
			public void onCreate(Connection connection) {
				Channel channel = connection.createChannel(false);
				channel.getConnection().addBlockedListener(new BlockedListener() {

					@Override
					public void handleUnblocked() throws IOException {
//						log.info("Queue is back to work. Start SMPP client.");
//						initialSmppClient.startSmppClient();
						log.info("Queue is connected. Start receving message.");
						ClientSmppSessionHandler.isQueueAvailable = true;
					}

					@Override
					public void handleBlocked(String reason) throws IOException {
//						log.info("Queue is full. Temporary stop SMPP client.");
//						initialSmppClient.stopSmppClient();
						log.warn("Queue is disconnected. Temporary stop receving message.");
						ClientSmppSessionHandler.isQueueAvailable = false;
					}
				});

				try {
					channel.close();
				} catch (Exception e) {
				}

				log.info("Queue is connected. Start smpp client.");
				initialSmppClient.startSmppClient();
				ClientSmppSessionHandler.isQueueAvailable = true;
			}

			@Override
			public void onClose(Connection connection) {
				onDisconnectedHandler();
			}

			@Override
			public void onShutDown(ShutdownSignalException signal) {
				onDisconnectedHandler();
			}

			@Override
			public void onFailed(Exception exception) {
				onDisconnectedHandler();
			}

			private void onDisconnectedHandler() {
//				log.info("Queue is disconnected. Temporary stop SMPP client.");
//				initialSmppClient.stopSmppClient();
				log.warn("Queue is disconnected. Temporary stop receving message.");
				ClientSmppSessionHandler.isQueueAvailable = false;
			}
		});
		configurer.configure(factory, connectionFactory);
		return factory;
	}

}
