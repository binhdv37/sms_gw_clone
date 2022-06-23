package com.dataServices.rabbitMq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.dataServices.services.IMessageHandlingDataService;

@Configuration
@Slf4j
public class ConfigDataServiceConsumerRabbitMq {

	private final IMessageHandlingDataService messageHandlingDataService;

	@Autowired
	public ConfigDataServiceConsumerRabbitMq(IMessageHandlingDataService messageHandlingDataService) {
		this.messageHandlingDataService = messageHandlingDataService;
	}

	@RabbitListener(queues = "${mbf.dataService.queueName}")
	public void listen(String in) {
		try {
			this.messageHandlingDataService.handleDataCommand(in, System.currentTimeMillis());
		} catch (Exception e) {
			log.error("Error while get message Data Service from queue with message: {} ", e.getMessage());
		}
	}
}
