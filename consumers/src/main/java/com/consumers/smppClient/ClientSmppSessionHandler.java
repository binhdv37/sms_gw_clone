package com.consumers.smppClient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppSession;
import com.cloudhopper.smpp.impl.DefaultSmppSessionHandler;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.DeliverSmResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.consumers.services.IMessageHandlingConsumersService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ClientSmppSessionHandler extends DefaultSmppSessionHandler {

	public static SmppSession session;
	public static boolean isQueueAvailable = true;
	private final IMessageHandlingConsumersService messageHandlingService;

	@Value("${mbf.submitSm.queueName}")
	private String SUBMITSM_QUEUENAME;

	@Value("${smpp.client.message.requeueWaitTime}")
	private int SMPP_MESSAGE_REQUEUE_WAIT_TIME = 5000;

	@Autowired
	public ClientSmppSessionHandler(IMessageHandlingConsumersService messageHandlingService) {
		this.messageHandlingService = messageHandlingService;
	}

	@Override
	public PduResponse firePduRequestReceived(PduRequest pduRequest) {
		log.trace("Go into firePduRequestReceived in Consumers");
		PduResponse pduResponse = pduRequest.createResponse();
		DeliverSmResp deliverSmResp;
		DeliverSm deliverSm;
		if (!isQueueAvailable) {
			pduResponse.setResultMessage("Unable to send message");
			pduResponse.setCommandStatus(SmppConstants.STATUS_SUBMITFAIL);
			return pduResponse;
		}
		try {
			if (pduRequest instanceof DeliverSm) {
				log.debug("PduRequest in Consumers is DeliverSm");
				long receiveDeliverSmDate = System.currentTimeMillis();
				deliverSm = (DeliverSm) pduRequest;
				deliverSmResp = messageHandlingService.putDeliverSmAndReceive(deliverSm, receiveDeliverSmDate);
				log.info("Receive deliverSmResp successful from queue");
				pduResponse = deliverSmResp;
			} else {
				log.debug("PduRequest in Consumers is not DeliverSm");
			}
			log.trace("Exit firePduRequestReceived in Consumers");
			return pduResponse;
		} catch (Exception e) {
			log.error("Error while response deliverSmResp to SMPP server with message: {}", e.getMessage());
			pduResponse.setResultMessage(e.getMessage());
			pduResponse.setCommandStatus(SmppConstants.STATUS_UNKNOWNERR);
			log.trace("Exit firePduRequestReceived in Consumers");
			return pduResponse;
		}
	}

	public String sendSubmitSmAndReceive(String strSubmitSm, long outQueueDate) {
		if (session == null || !session.isBound()) {
			requeueAndReconnect(strSubmitSm);
			return null;
		}
		try {
			String result = messageHandlingService.sendSubmitSmMbf(session, strSubmitSm, outQueueDate);
			return result;
		} catch (Exception e) {
			requeueAndReconnect(strSubmitSm);
			return null;
		}
	}

	private String requeueAndReconnect(String strSubmitSm) {
		try {
			log.warn("SMPP Session is closed");
			String result = messageHandlingService.sendSubmitSmBackToQueue(strSubmitSm, System.currentTimeMillis());
			Thread.sleep(SMPP_MESSAGE_REQUEUE_WAIT_TIME);
			return result;
		} catch (Exception e) {
			log.error("Error while sending SubmitSM back to queue with message: {}", e.getMessage());
			return null;
		}
	}

}
