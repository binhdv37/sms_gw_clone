package com.consumers.rabbitMq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.consumers.smppClient.ClientSmppSessionHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ConfigListenerSubmitSmRabbitMq {

	private final ClientSmppSessionHandler clientSmppSessionHandler;

	@Autowired
	public ConfigListenerSubmitSmRabbitMq(ClientSmppSessionHandler clientSmppSessionHandler) {
		this.clientSmppSessionHandler = clientSmppSessionHandler;
	}

	@RabbitListener(queues = "${mbf.submitSm.queueName}")
	public void listen(String in) {
		long outQueueDate = System.currentTimeMillis();
		try {
			clientSmppSessionHandler.sendSubmitSmAndReceive(in, outQueueDate);
		} catch (Exception e) {
			log.warn("Error while get message submitSm from queue with message: " + e.getMessage());
		}
	}

	@RabbitListener(queues = "${mbf.submitSmWeb.queueName}")
	public String listenToWebSms(String in) {
		String out = "";
		long outQueueDate = System.currentTimeMillis();
		try {
			out = clientSmppSessionHandler.sendSubmitSmAndReceive(in, outQueueDate);
		} catch (Exception e) {
			log.warn("Error while get message submitSm from queue with message: " + e.getMessage());
		}
		return out;
	}

}
