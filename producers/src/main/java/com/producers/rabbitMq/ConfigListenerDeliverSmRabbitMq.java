package com.producers.rabbitMq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.producers.services.IMessageHandlingMasterDataChanged;
import com.producers.smppServer.DftSmppSessionHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class ConfigListenerDeliverSmRabbitMq {

	private final DftSmppSessionHandler dftSmppSessionHandler;

	private final IMessageHandlingMasterDataChanged messageHandlingMasterDataChanged;

	@Autowired
	public ConfigListenerDeliverSmRabbitMq(DftSmppSessionHandler dftSmppSessionHandler,
			IMessageHandlingMasterDataChanged messageHandlingMasterDataChanged) {
		this.dftSmppSessionHandler = dftSmppSessionHandler;
		this.messageHandlingMasterDataChanged = messageHandlingMasterDataChanged;
	}

	@RabbitListener(queues = "${mbf.deliverSm.queueName}")
	public String listen(String in) {
		String out = "";
		try {
			out = dftSmppSessionHandler.sendDeliverSmAndReceive(in, System.currentTimeMillis());
		} catch (Exception e) {
			log.error("Error while get message deliverSm from queue with message: " + e.getMessage());
		}
		return out;
	}

	@RabbitListener(queues = "${mbf.submitSmResp.queueName}")
	public void listenSubmitSmResp(String in) {
		try {
			dftSmppSessionHandler.sendSubmitSmResponsePdu(in, System.currentTimeMillis());
		} catch (Exception e) {
			log.error("Error while get message SubmitSm reponse from queue with message: " + e.getMessage());
		}
	}

	@RabbitListener(queues = "${mbf.masterDataProducer.queueName}")
	public String listenToMasterDataChanged(String in) {
		String out = "";
		try {
			out = messageHandlingMasterDataChanged.handleMessage(in, System.currentTimeMillis());
		} catch (Exception e) {
			log.error("Error while get message submitSm from queue with message: " + e.getMessage());
		}
		return out;
	}
}
