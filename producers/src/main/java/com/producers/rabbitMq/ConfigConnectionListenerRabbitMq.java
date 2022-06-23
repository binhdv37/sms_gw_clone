package com.producers.rabbitMq;

import java.io.IOException;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.producers.smppServer.DftSmppSessionHandler;
import com.producers.smppServer.InitialSmppServer;
import com.rabbitmq.client.BlockedListener;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ShutdownSignalException;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ConfigConnectionListenerRabbitMq {

	@Autowired
	private InitialSmppServer initialSmppServer;
	
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
//						log.info("Queue is back to work. Start SMPP server.");
//						initialSmppServer.startSmppServer();
						log.info("Queue is connected. Start receving message.");
						DftSmppSessionHandler.isQueueAvailable = true;
					}

					@Override
					public void handleBlocked(String reason) throws IOException {
//						log.warn("Queue is full. Temporary stop SMPP server.");
//						initialSmppServer.stopSmppServer();
						log.warn("Queue is disconnected. Temporary stop receving message.");
						DftSmppSessionHandler.isQueueAvailable = false;
					}
				});

				try {
					channel.close();
				} catch (Exception e) {
				}

				log.info("Queue is connected. Start smpp server.");
				initialSmppServer.startSmppServer();
				DftSmppSessionHandler.isQueueAvailable = true;
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
//				log.warn("Queue is disconnected. Temporary stop SMPP server.");
//				initialSmppServer.stopSmppServer();
				log.warn("Queue is disconnected. Temporary stop receving message.");
				DftSmppSessionHandler.isQueueAvailable = false;
			}
		});
		configurer.configure(factory, connectionFactory);
		return factory;
	}
	
}
